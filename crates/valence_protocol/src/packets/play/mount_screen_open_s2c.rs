use valence_binary::{Decode, Encode, VarInt};

use crate::Packet;

// TODO(26.1): renamed from horse_screen_open. Mojang's
// `ClientboundMountScreenOpenPacket` carries `containerId`, `inventoryColumns`,
// and `entityId` (all `int`). The wire encoding has not been re-verified
// against the new `StreamCodec`; the field types below mirror the pre-26.1
// packet so the protocol crate compiles. Audit before relying on the wire
// format.
#[derive(Copy, Clone, Debug, Encode, Decode, Packet)]
pub struct MountScreenOpenS2c {
    pub window_id: u8,
    pub slot_count: VarInt,
    pub entity_id: i32,
}
