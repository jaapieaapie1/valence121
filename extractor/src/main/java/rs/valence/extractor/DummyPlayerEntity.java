package rs.valence.extractor;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.ProfilePublicKey;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class DummyPlayerEntity extends Player {

    public static final DummyPlayerEntity INSTANCE;

    static {
        INSTANCE = Main.magicallyInstantiate(DummyPlayerEntity.class);

        // TODO(26.1): yarn `initDataTracker` is Mojang `defineSynchedData`; verify
        // SynchedEntityData.Builder constructor still takes a SyncedDataHolder/Entity.
        INSTANCE.defineSynchedData(new SynchedEntityData.Builder(INSTANCE));
    }

    public DummyPlayerEntity(
        Level world,
        BlockPos pos,
        float yaw,
        GameProfile gameProfile,
        @Nullable ProfilePublicKey publicKey
    ) {
        super(world, gameProfile);
    }

    @Override
    public GameType gameMode() {
        return GameType.SURVIVAL;
    }

    @Override
    public boolean isSpectator() {
        return false;
    }

    @Override
    public boolean isCreative() {
        return false;
    }
}
