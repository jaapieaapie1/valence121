package rs.valence.extractor;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ExplosionParticleInfo;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.Difficulty;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import net.minecraft.world.clock.ClockManager;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragonPart;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.RecipeAccess;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.LevelTickAccess;
import org.jetbrains.annotations.Nullable;

public class DummyWorld extends Level {

    public static final DummyWorld INSTANCE;

    static {
        INSTANCE = Main.magicallyInstantiate(DummyWorld.class);

        try {
            var randomField = Level.class.getDeclaredField("random");
            randomField.setAccessible(true);
            randomField.set(INSTANCE, RandomSource.create());

            var propertiesField = Level.class.getDeclaredField("levelData");
            propertiesField.setAccessible(true);
            propertiesField.set(INSTANCE, new DummyWritableLevelData());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private DummyWorld(
        WritableLevelData properties,
        ResourceKey<Level> registryRef,
        RegistryAccess registryManager,
        Holder<DimensionType> dimension,
        Supplier<ProfilerFiller> profiler,
        boolean isClient,
        boolean debugWorld,
        long seed,
        int maxChainedNeighborUpdates
    ) {
        super(
            properties,
            registryRef,
            registryManager,
            dimension,
            isClient,
            debugWorld,
            seed,
            maxChainedNeighborUpdates
        );
    }

    @Override
    public void sendBlockUpdated(
        BlockPos pos,
        BlockState oldState,
        BlockState newState,
        int flags
    ) {}

    @Override
    public void playSeededSound(
        @Nullable Entity source,
        double x,
        double y,
        double z,
        Holder<SoundEvent> sound,
        SoundSource category,
        float volume,
        float pitch,
        long seed
    ) {}

    @Override
    public void playSeededSound(
        @Nullable Entity source,
        Entity entity,
        Holder<SoundEvent> sound,
        SoundSource category,
        float volume,
        float pitch,
        long seed
    ) {}

    @Nullable
    @Override
    public Entity getEntity(int id) {
        return null;
    }

    @Override
    public TickRateManager tickRateManager() {
        return null;
    }

    @Nullable
    @Override
    public MapItemSavedData getMapData(MapId id) {
        return null;
    }

    @Override
    public void destroyBlockProgress(
        int entityId,
        BlockPos pos,
        int progress
    ) {}

    @Override
    public Scoreboard getScoreboard() {
        return new Scoreboard();
    }

    @Override
    public RecipeAccess recipeAccess() {
        return null;
    }

    @Override
    public ClockManager clockManager() {
        return null;
    }

    @Override
    public EnvironmentAttributeSystem environmentAttributes() {
        return null;
    }

    @Override
    public Collection<EnderDragonPart> dragonParts() {
        return List.of();
    }

    @Override
    protected LevelEntityGetter<Entity> getEntities() {
        return null;
    }

    @Override
    public LevelTickAccess<Block> getBlockTicks() {
        return null;
    }

    @Override
    public LevelTickAccess<Fluid> getFluidTicks() {
        return null;
    }

    @Override
    public ChunkSource getChunkSource() {
        return null;
    }

    @Override
    public void gameEvent(
        Holder<net.minecraft.world.level.gameevent.GameEvent> event,
        net.minecraft.world.phys.Vec3 emitterPos,
        net.minecraft.world.level.gameevent.GameEvent.Context emitter
    ) {}

    @Override
    public void levelEvent(@Nullable Entity source, int eventId, BlockPos pos, int data) {}

    @Override
    public PotionBrewing potionBrewing() {
        return null;
    }

    @Override
    public FeatureFlagSet enabledFeatures() {
        return FeatureFlagSet.of();
    }

    @Override
    public FuelValues fuelValues() {
        return null;
    }

    @Override
    public void explode(
        @Nullable Entity entity,
        @Nullable DamageSource damageSource,
        @Nullable ExplosionDamageCalculator behavior,
        double x,
        double y,
        double z,
        float power,
        boolean createFire,
        Level.ExplosionInteraction explosionSourceType,
        ParticleOptions smallParticle,
        ParticleOptions largeParticle,
        WeightedList<ExplosionParticleInfo> explosionParticles,
        Holder<SoundEvent> soundEvent
    ) {}

    @Override
    public int getSeaLevel() {
        return 0;
    }

    @Override
    public List<? extends Player> players() {
        return List.of();
    }

    @Override
    public Holder<Biome> getUncachedNoiseBiome(
        int biomeX,
        int biomeY,
        int biomeZ
    ) {
        return null;
    }

    @Override
    public String gatherChunkSourceStats() {
        return "";
    }

    @Override
    public void setRespawnData(LevelData.RespawnData data) {}

    @Override
    public LevelData.RespawnData getRespawnData() {
        return LevelData.RespawnData.DEFAULT;
    }

    @Override
    public long nextSubTickCount() {
        return 0;
    }

    @Override
    public net.minecraft.world.level.border.WorldBorder getWorldBorder() {
        return null;
    }

    private static class DummyWritableLevelData implements WritableLevelData {

        @Override
        public LevelData.RespawnData getRespawnData() {
            return LevelData.RespawnData.DEFAULT;
        }

        @Override
        public long getGameTime() {
            return 0;
        }

        @Override
        public boolean isHardcore() {
            return false;
        }

        @Override
        public Difficulty getDifficulty() {
            return Difficulty.PEACEFUL;
        }

        @Override
        public boolean isDifficultyLocked() {
            return false;
        }

        @Override
        public void setSpawn(LevelData.RespawnData data) {}
    }
}
