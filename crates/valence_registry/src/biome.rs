//! Contains biomes and the biome registry. Minecraft's default biomes are added
//! to the registry by default.
//!
//! ### **NOTE:**
//! - Modifying the biome registry after the server has started can break
//!   invariants within instances and clients! Make sure there are no instances
//!   or clients spawned before mutating.
//! - A biome named "minecraft:plains" must exist. Otherwise, vanilla clients
//!   will be disconnected.

use std::collections::BTreeMap;
use std::ops::{Deref, DerefMut};

use bevy_app::prelude::*;
use bevy_ecs::prelude::*;
use serde::{Deserialize, Serialize};
use tracing::error;
use valence_ident::{ident, Ident};
use valence_nbt::serde::ser::CompoundSerializer;

use crate::codec::{RegistryCodec, RegistryValue};
use crate::{Registry, RegistryIdx, RegistrySet};

pub struct BiomePlugin;

impl Plugin for BiomePlugin {
    fn build(&self, app: &mut App) {
        app.init_resource::<BiomeRegistry>()
            .add_systems(PreStartup, load_default_biomes)
            .add_systems(PostUpdate, update_biome_registry.before(RegistrySet));
    }
}

fn load_default_biomes(mut reg: ResMut<BiomeRegistry>, codec: Res<RegistryCodec>) {
    let mut helper = move || -> anyhow::Result<()> {
        for value in codec.registry(BiomeRegistry::KEY) {
            let biome = Biome::deserialize(value.element.clone())?;

            reg.insert(value.name.clone(), biome);
        }

        // Move "plains" to the front so that `BiomeId::default()` is the ID of plains.
        reg.swap_to_front(ident!("plains"));

        Ok(())
    };

    if let Err(e) = helper() {
        error!("failed to load default biomes from registry codec: {e:#}");
    }
}

fn update_biome_registry(reg: Res<BiomeRegistry>, mut codec: ResMut<RegistryCodec>) {
    if reg.is_changed() {
        let biomes = codec.registry_mut(BiomeRegistry::KEY);

        biomes.clear();

        biomes.extend(reg.iter().map(|(_, name, biome)| {
            RegistryValue {
                name: name.into(),
                element: biome
                    .serialize(CompoundSerializer)
                    .expect("failed to serialize biome"),
            }
        }));
    }
}

#[derive(Resource, Default, Debug)]
pub struct BiomeRegistry {
    reg: Registry<BiomeId, Biome>,
}

impl BiomeRegistry {
    pub const KEY: Ident<&'static str> = ident!("worldgen/biome");
}

impl Deref for BiomeRegistry {
    type Target = Registry<BiomeId, Biome>;

    fn deref(&self) -> &Self::Target {
        &self.reg
    }
}

impl DerefMut for BiomeRegistry {
    fn deref_mut(&mut self) -> &mut Self::Target {
        &mut self.reg
    }
}

#[derive(Copy, Clone, PartialEq, Eq, PartialOrd, Ord, Hash, Default, Debug)]
pub struct BiomeId(u32);

impl BiomeId {
    pub const DEFAULT: Self = BiomeId(0);
}

impl RegistryIdx for BiomeId {
    const MAX: usize = u32::MAX as usize;

    #[inline]
    fn to_index(self) -> usize {
        self.0 as usize
    }

    #[inline]
    fn from_index(idx: usize) -> Self {
        Self(idx as u32)
    }
}

#[derive(Serialize, Deserialize, Clone, Debug)]
pub struct Biome {
    pub has_precipitation: bool,
    pub temperature: f32,
    pub downfall: f32,
    #[serde(skip_serializing_if = "Option::is_none", default)]
    pub temperature_modifier: Option<String>,
    #[serde(default)]
    pub effects: BiomeEffects,
    /// 26.1 introduced a free-form `attributes` map containing visual/audio
    /// settings under namespaced keys (e.g. `minecraft:visual/sky_color`).
    /// Stored opaquely so we can round-trip without modeling every variant.
    #[serde(default, skip_serializing_if = "BTreeMap::is_empty")]
    pub attributes: BTreeMap<String, serde_json::Value>,
}

impl Default for Biome {
    /// Default will be the same as the `minecraft:plains` biome.
    fn default() -> Self {
        Self {
            has_precipitation: true,
            temperature: 0.8,
            downfall: 0.4,
            temperature_modifier: None,
            effects: BiomeEffects::default(),
            attributes: BTreeMap::new(),
        }
    }
}

#[derive(Serialize, Deserialize, Clone, Debug, Default)]
pub struct BiomeEffects {
    /// Water tint, packed as ARGB. In 26.1 the registry codec stores this as a
    /// hex string (`"#3f76e4"`); we deserialize that into a `u32`.
    #[serde(
        with = "hex_color_opt",
        skip_serializing_if = "Option::is_none",
        default
    )]
    pub water_color: Option<u32>,
    #[serde(
        with = "hex_color_opt",
        skip_serializing_if = "Option::is_none",
        default
    )]
    pub foliage_color: Option<u32>,
    #[serde(
        with = "hex_color_opt",
        skip_serializing_if = "Option::is_none",
        default
    )]
    pub grass_color: Option<u32>,
    #[serde(
        with = "hex_color_opt",
        skip_serializing_if = "Option::is_none",
        default
    )]
    pub dry_foliage_color: Option<u32>,
    #[serde(skip_serializing_if = "Option::is_none", default)]
    pub grass_color_modifier: Option<String>,
}

/// Serde helper for the new 26.1 hex-string color format.
mod hex_color_opt {
    use serde::de::{Error, Unexpected};
    use serde::{Deserialize, Deserializer, Serializer};

    pub(super) fn deserialize<'de, D>(d: D) -> Result<Option<u32>, D::Error>
    where
        D: Deserializer<'de>,
    {
        // Accept either a hex string ("#aabbcc" / "#aabbccdd") or a raw integer
        // so user code that constructs colors numerically still round-trips.
        #[derive(Deserialize)]
        #[serde(untagged)]
        enum Repr {
            Str(String),
            Int(u32),
        }

        match Option::<Repr>::deserialize(d)? {
            None => Ok(None),
            Some(Repr::Int(v)) => Ok(Some(v)),
            Some(Repr::Str(s)) => {
                let hex = s.strip_prefix('#').unwrap_or(&s);
                u32::from_str_radix(hex, 16).map(Some).map_err(|_| {
                    D::Error::invalid_value(Unexpected::Str(&s), &"hex color like \"#rrggbb\"")
                })
            }
        }
    }

    pub(super) fn serialize<S>(value: &Option<u32>, s: S) -> Result<S::Ok, S::Error>
    where
        S: Serializer,
    {
        // `skip_serializing_if = "Option::is_none"` keeps None out of the
        // serialized form, so we only have to handle Some here.
        match value {
            Some(v) => s.serialize_str(&format!("#{v:08x}")),
            None => s.serialize_none(),
        }
    }
}
