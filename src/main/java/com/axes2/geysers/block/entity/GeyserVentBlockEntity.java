package com.axes2.geysers.block.entity;

import com.axes2.geysers.config.GeysersClientConfig;
import com.axes2.geysers.eruption.GeyserEruption;
import com.axes2.geysers.eruption.GeyserPhase;
import com.axes2.geysers.eruption.GeyserStyle;
import com.axes2.geysers.eruption.GeyserStyles;
import com.axes2.geysers.registry.ModBlockEntities;
import com.axes2.geysers.registry.ModParticles;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Owns the authoritative eruption state on the server: the current {@link GeyserPhase},
 * when it started, the intensity it started at, and the active style id. The client is
 * synced only on phase changes and re-derives the continuous intensity itself.
 */
public class GeyserVentBlockEntity extends BlockEntity {
    private GeyserPhase phase = GeyserPhase.DORMANT;
    private long phaseStartGameTime = 0L;
    private float phaseStartIntensity = 0f;
    private ResourceLocation styleId = GeyserStyles.DEFAULT_ID;
    /** When >= 0, intensity is pinned to this value and the machine is paused (tuning aid). */
    private float manualIntensity = -1f;

    public GeyserVentBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GEYSER_VENT.get(), pos, state);
    }

    public GeyserStyle style() {
        return GeyserStyles.get(styleId);
    }

    public GeyserPhase phase() {
        return phase;
    }

    public ResourceLocation styleId() {
        return styleId;
    }

    public boolean isManual() {
        return manualIntensity >= 0f;
    }

    /** Current intensity (0..1) — the single value every emitter/renderer/sound scales off. */
    public float intensity(long gameTime) {
        if (manualIntensity >= 0f) {
            return manualIntensity;
        }
        long elapsed = gameTime - phaseStartGameTime;
        return GeyserEruption.computeIntensity(style(), phase, elapsed, phaseStartIntensity);
    }

    // ---- server tick ----

    public void serverTick(Level level, BlockPos pos, BlockState state) {
        if (manualIntensity >= 0f || phase == GeyserPhase.DORMANT) {
            return;
        }
        long gameTime = level.getGameTime();
        long elapsed = gameTime - phaseStartGameTime;
        GeyserStyle style = style();
        int duration = style.phase(phase).durationTicks();
        if (duration > 0 && elapsed >= duration) {
            float endIntensity = GeyserEruption.computeIntensity(style, phase, elapsed, phaseStartIntensity);
            GeyserPhase next = GeyserEruption.nextPhase(phase, style);
            setPhaseInternal(next, gameTime, endIntensity);
            sync();
        }
    }

    private void setPhaseInternal(GeyserPhase newPhase, long gameTime, float startIntensity) {
        this.phase = newPhase;
        this.phaseStartGameTime = gameTime;
        this.phaseStartIntensity = startIntensity;
    }

    // ---- client tick (particle emission, M2a: particle-first) ----

    /**
     * Emits the particle layers each client tick, scaled by the current intensity, the
     * style's particle-mix weights, and the client density config. Everything reads off
     * the single {@code intensity} value so the visuals stay continuous.
     */
    public void clientTick(Level level, BlockPos pos, BlockState state) {
        long gameTime = level.getGameTime();
        float intensity = intensity(gameTime);
        RandomSource random = level.getRandom();

        if (intensity <= 0.001f && phase == GeyserPhase.DORMANT) {
            if (random.nextFloat() < 0.04f) {
                spawnSteam(level, pos, random, 0.12f);
            }
            return;
        }

        GeyserStyle style = style();
        double density = GeysersClientConfig.densityMultiplier();
        int budget = GeysersClientConfig.maxParticlesPerVentPerTick();

        int steamCount = (int) Math.ceil(intensity * 3.0 * style.steamWeight() * density);
        for (int k = 0; k < steamCount && budget > 0; k++, budget--) {
            spawnSteam(level, pos, random, intensity);
        }

        int mistCount = (int) Math.ceil(intensity * 2.0 * style.mistWeight() * density);
        for (int k = 0; k < mistCount && budget > 0; k++, budget--) {
            spawnMist(level, pos, random, intensity);
        }

        if (phase == GeyserPhase.PRIMING) {
            int bubbleCount = (int) Math.ceil(intensity * 4.0 * density);
            for (int k = 0; k < bubbleCount && budget > 0; k++, budget--) {
                spawnBubble(level, pos, random, style);
            }
        }

        if (phase == GeyserPhase.SURGE || phase == GeyserPhase.FULL
                || phase == GeyserPhase.DECLINE || phase == GeyserPhase.STEAM) {
            int sprayCount = (int) Math.ceil(intensity * 6.0 * style.sprayWeight() * density);
            for (int k = 0; k < sprayCount && budget > 0; k++, budget--) {
                spawnSpray(level, pos, random, style, intensity);
            }
        }
    }

    private void spawnSteam(Level level, BlockPos pos, RandomSource random, float intensity) {
        double x = pos.getX() + 0.5 + spread(random, 0.35f);
        double y = pos.getY() + 1.0;
        double z = pos.getZ() + 0.5 + spread(random, 0.35f);
        double dy = 0.04 + 0.06 * intensity;
        level.addParticle(ModParticles.STEAM.get(), x, y, z, spread(random, 0.02f), dy, spread(random, 0.02f));
    }

    private void spawnMist(Level level, BlockPos pos, RandomSource random, float intensity) {
        double x = pos.getX() + 0.5 + spread(random, 0.6f);
        double y = pos.getY() + 1.0 + random.nextFloat() * 0.5;
        double z = pos.getZ() + 0.5 + spread(random, 0.6f);
        level.addParticle(ModParticles.MIST.get(), x, y, z, spread(random, 0.03f), 0.01, spread(random, 0.03f));
    }

    private void spawnBubble(Level level, BlockPos pos, RandomSource random, GeyserStyle style) {
        double r = style.columnRadius();
        double x = pos.getX() + 0.5 + spread(random, (float) r);
        double y = pos.getY() + 0.85;
        double z = pos.getZ() + 0.5 + spread(random, (float) r);
        level.addParticle(ModParticles.BUBBLE.get(), x, y, z, 0.0, 0.03, 0.0);
    }

    private void spawnSpray(Level level, BlockPos pos, RandomSource random, GeyserStyle style, float intensity) {
        double radius = style.columnRadius();
        double x = pos.getX() + 0.5 + spread(random, (float) radius);
        double y = pos.getY() + 1.0;
        double z = pos.getZ() + 0.5 + spread(random, (float) radius);
        double heightScale = style.maxColumnHeight() / 8.0;
        double dy = (0.35 + 0.85 * intensity) * heightScale;
        double lateral = 0.12 * style.burstiness();
        level.addParticle(ModParticles.SPRAY.get(), x, y, z,
                spread(random, (float) lateral), dy, spread(random, (float) lateral));
    }

    /** Symmetric jitter in [-amount, amount]. */
    private static double spread(RandomSource random, float amount) {
        return (random.nextFloat() - 0.5f) * 2.0f * amount;
    }

    // ---- command-driven controls (server side) ----

    public void trigger(long gameTime) {
        this.manualIntensity = -1f;
        setPhaseInternal(GeyserPhase.PRIMING, gameTime, 0f);
        sync();
    }

    public void forcePhase(GeyserPhase newPhase, long gameTime) {
        float current = intensity(gameTime);
        this.manualIntensity = -1f;
        setPhaseInternal(newPhase, gameTime, current);
        sync();
    }

    public void reset(long gameTime) {
        this.manualIntensity = -1f;
        setPhaseInternal(GeyserPhase.DORMANT, gameTime, 0f);
        sync();
    }

    public void setManualIntensity(float value) {
        this.manualIntensity = Math.max(0f, Math.min(1f, value));
        sync();
    }

    public void clearManual(long gameTime) {
        this.manualIntensity = -1f;
        // Restart the current phase's timing from now so the cycle continues smoothly.
        setPhaseInternal(phase, gameTime, 0f);
        sync();
    }

    public boolean setStyle(ResourceLocation id) {
        if (!GeyserStyles.exists(id)) {
            return false;
        }
        this.styleId = id;
        sync();
        return true;
    }

    private void sync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    // ---- persistence + client sync ----

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("Phase", phase.getSerializedName());
        tag.putLong("PhaseStart", phaseStartGameTime);
        tag.putFloat("PhaseStartIntensity", phaseStartIntensity);
        tag.putString("Style", styleId.toString());
        tag.putFloat("ManualIntensity", manualIntensity);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        GeyserPhase loaded = GeyserPhase.byName(tag.getString("Phase"));
        this.phase = loaded == null ? GeyserPhase.DORMANT : loaded;
        this.phaseStartGameTime = tag.getLong("PhaseStart");
        this.phaseStartIntensity = tag.getFloat("PhaseStartIntensity");
        ResourceLocation id = ResourceLocation.tryParse(tag.getString("Style"));
        this.styleId = id == null ? GeyserStyles.DEFAULT_ID : id;
        this.manualIntensity = tag.contains("ManualIntensity") ? tag.getFloat("ManualIntensity") : -1f;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet, HolderLookup.Provider registries) {
        CompoundTag tag = packet.getTag();
        if (tag != null) {
            loadAdditional(tag, registries);
        }
    }
}
