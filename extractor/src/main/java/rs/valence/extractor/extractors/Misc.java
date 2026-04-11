package rs.valence.extractor.extractors;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.lang.reflect.Modifier;
import java.util.Locale;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.animal.golem.CopperGolemState;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.level.block.WeatheringCopper;
import rs.valence.extractor.Main;

public class Misc implements Main.Extractor {

    private final RegistryAccess.Frozen registryManager;

    public Misc(MinecraftServer server) {
        this.registryManager = server.registryAccess();
    }

    @Override
    public String fileName() {
        return "misc.json";
    }

    /**
     * Iterate a dynamic registry by its ResourceKey, writing each entry's
     * path + raw id into the given JsonObject. The lookupOrThrow result is
     * downcast to Registry<T> so we can access the raw id - in vanilla the
     * backing implementation (MappedRegistry) implements Registry, which
     * extends HolderLookup.RegistryLookup.
     */
    private <T> void writeDynamicRegistry(
        JsonObject json,
        ResourceKey<? extends net.minecraft.core.Registry<T>> key
    ) {
        @SuppressWarnings("unchecked")
        Registry<T> registry =
            (Registry<T>) registryManager.lookupOrThrow(key);
        for (T value : registry) {
            json.addProperty(
                registry.getKey(value).getPath(),
                registry.getId(value)
            );
        }
    }

    @Override
    public JsonElement extract() throws Exception {
        var miscJson = new JsonObject();

        var entityTypeJson = new JsonObject();
        for (var type : BuiltInRegistries.ENTITY_TYPE) {
            entityTypeJson.addProperty(
                BuiltInRegistries.ENTITY_TYPE.getKey(type).getPath(),
                BuiltInRegistries.ENTITY_TYPE.getId(type)
            );
        }
        miscJson.add("entity_type", entityTypeJson);

        var entityStatusJson = new JsonObject();
        // TODO(26.1): yarn EntityStatuses mapped to Mojang EntityEvent. The
        // old yarn-specific "field_30030" special case is dropped because the
        // Mojang class uses a real field name.
        for (var field : EntityEvent.class.getDeclaredFields()) {
            field.setAccessible(true);
            if (
                Modifier.isStatic(field.getModifiers()) &&
                field.canAccess(null) &&
                field.get(null) instanceof Byte code
            ) {
                entityStatusJson.addProperty(
                    field.getName().toLowerCase(Locale.ROOT),
                    code
                );
            }
        }
        miscJson.add("entity_status", entityStatusJson);

        var entityAnimationJson = new JsonObject();
        for (var field : ClientboundAnimatePacket.class.getDeclaredFields()) {
            field.setAccessible(true);
            if (
                Modifier.isStatic(field.getModifiers()) &&
                field.canAccess(null) &&
                field.get(null) instanceof Integer i
            ) {
                entityAnimationJson.addProperty(
                    field.getName().toLowerCase(Locale.ROOT),
                    i
                );
            }
        }
        miscJson.add("entity_animation", entityAnimationJson);

        var villagerTypeJson = new JsonObject();
        writeDynamicRegistry(villagerTypeJson, Registries.VILLAGER_TYPE);
        miscJson.add("villager_type", villagerTypeJson);

        var villagerProfessionJson = new JsonObject();
        writeDynamicRegistry(
            villagerProfessionJson,
            Registries.VILLAGER_PROFESSION
        );
        miscJson.add("villager_profession", villagerProfessionJson);

        var catVariantJson = new JsonObject();
        writeDynamicRegistry(catVariantJson, Registries.CAT_VARIANT);
        miscJson.add("cat_variant", catVariantJson);

        var frogVariantJson = new JsonObject();
        writeDynamicRegistry(frogVariantJson, Registries.FROG_VARIANT);
        miscJson.add("frog_variant", frogVariantJson);

        var wolfVariantJson = new JsonObject();
        writeDynamicRegistry(wolfVariantJson, Registries.WOLF_VARIANT);
        miscJson.add("wolf_variant", wolfVariantJson);

        var pigVariant = new JsonObject();
        writeDynamicRegistry(pigVariant, Registries.PIG_VARIANT);
        miscJson.add("pig_variant", pigVariant);

        var cowVariant = new JsonObject();
        writeDynamicRegistry(cowVariant, Registries.COW_VARIANT);
        miscJson.add("cow_variant", cowVariant);

        var chickenVariant = new JsonObject();
        writeDynamicRegistry(chickenVariant, Registries.CHICKEN_VARIANT);
        miscJson.add("chicken_variant", chickenVariant);

        var paintingVariant = new JsonObject();
        writeDynamicRegistry(paintingVariant, Registries.PAINTING_VARIANT);
        miscJson.add("painting_variant", paintingVariant);

        var wolfSoundVariant = new JsonObject();
        writeDynamicRegistry(wolfSoundVariant, Registries.WOLF_SOUND_VARIANT);
        miscJson.add("wolf_sound_variant", wolfSoundVariant);

        var catSoundVariant = new JsonObject();
        writeDynamicRegistry(catSoundVariant, Registries.CAT_SOUND_VARIANT);
        miscJson.add("cat_sound_variant", catSoundVariant);

        var cowSoundVariant = new JsonObject();
        writeDynamicRegistry(cowSoundVariant, Registries.COW_SOUND_VARIANT);
        miscJson.add("cow_sound_variant", cowSoundVariant);

        var chickenSoundVariant = new JsonObject();
        writeDynamicRegistry(chickenSoundVariant, Registries.CHICKEN_SOUND_VARIANT);
        miscJson.add("chicken_sound_variant", chickenSoundVariant);

        var pigSoundVariant = new JsonObject();
        writeDynamicRegistry(pigSoundVariant, Registries.PIG_SOUND_VARIANT);
        miscJson.add("pig_sound_variant", pigSoundVariant);

        var zombieNautilusVariant = new JsonObject();
        writeDynamicRegistry(
            zombieNautilusVariant,
            Registries.ZOMBIE_NAUTILUS_VARIANT
        );
        miscJson.add("zombie_nautilus_variant", zombieNautilusVariant);

        var directionJson = new JsonObject();
        for (var dir : Direction.values()) {
            // yarn Direction.getId() -> Mojang Direction.get3DDataValue()
            directionJson.addProperty(dir.name(), dir.get3DDataValue());
        }
        miscJson.add("direction", directionJson);

        var entityPoseJson = new JsonObject();
        var poses = Pose.values();
        for (int i = 0; i < poses.length; i++) {
            entityPoseJson.addProperty(
                poses[i].name().toLowerCase(Locale.ROOT),
                i
            );
        }
        miscJson.add("entity_pose", entityPoseJson);

        var particleTypesJson = new JsonObject();
        for (var type : BuiltInRegistries.PARTICLE_TYPE) {
            particleTypesJson.addProperty(
                BuiltInRegistries.PARTICLE_TYPE.getKey(type).getPath(),
                BuiltInRegistries.PARTICLE_TYPE.getId(type)
            );
        }
        miscJson.add("particle_type", particleTypesJson);

        var snifferStateJson = new JsonObject();
        for (var state : Sniffer.State.values()) {
            snifferStateJson.addProperty(
                state.name().toLowerCase(Locale.ROOT),
                state.ordinal()
            );
        }
        miscJson.add("sniffer_state", snifferStateJson);

        var armadilloStateJson = new JsonObject();
        for (var state : Armadillo.ArmadilloState.values()) {
            armadilloStateJson.addProperty(
                state.name().toLowerCase(Locale.ROOT),
                state.ordinal()
            );
        }
        miscJson.add("armadillo_state", armadilloStateJson);

        var copperGolemStateJson = new JsonObject();
        for (var state : CopperGolemState.values()) {
            copperGolemStateJson.addProperty(
                state.name().toLowerCase(Locale.ROOT),
                state.ordinal()
            );
        }
        miscJson.add("copper_golem_state", copperGolemStateJson);

        var weatheringCopperStateJson = new JsonObject();
        for (var state : WeatheringCopper.WeatherState.values()) {
            weatheringCopperStateJson.addProperty(
                state.name().toLowerCase(Locale.ROOT),
                state.ordinal()
            );
        }
        miscJson.add("weathering_copper_state", weatheringCopperStateJson);

        var humanoidArmJson = new JsonObject();
        for (var arm : HumanoidArm.values()) {
            humanoidArmJson.addProperty(
                arm.name().toLowerCase(Locale.ROOT),
                arm.ordinal()
            );
        }
        miscJson.add("humanoid_arm", humanoidArmJson);

        var trackedDataHandlerJson = new JsonObject();
        for (var field : EntityDataSerializers.class.getDeclaredFields()) {
            field.setAccessible(true);
            if (
                Modifier.isStatic(field.getModifiers()) &&
                field.get(null) instanceof EntityDataSerializer<?> handler
            ) {
                var name = field.getName().toLowerCase(Locale.ROOT);
                // TODO(26.1): verify EntityDataSerializers.getSerializedId is
                // the correct Mojang name for yarn TrackedDataHandlerRegistry.getId
                var id = EntityDataSerializers.getSerializedId(handler);

                trackedDataHandlerJson.addProperty(name, id);
            }
        }
        miscJson.add("tracked_data_handler", trackedDataHandlerJson);

        return miscJson;
    }
}
