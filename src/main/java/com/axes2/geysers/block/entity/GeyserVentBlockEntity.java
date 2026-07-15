package com.axes2.geysers.block.entity;

import com.axes2.geysers.eruption.GeyserEruption;
import com.axes2.geysers.eruption.GeyserPhase;
import com.axes2.geysers.eruption.GeyserStyle;
import com.axes2.geysers.eruption.GeyserStyles;
import com.axes2.geysers.registry.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
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
