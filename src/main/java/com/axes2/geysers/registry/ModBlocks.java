package com.axes2.geysers.registry;

import com.axes2.geysers.Geysers;
import com.axes2.geysers.block.GeyserVentBlock;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Block registry.
 */
public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Geysers.MODID);

    /** The debug-triggerable geyser vent that hosts the eruption state machine. */
    public static final DeferredBlock<GeyserVentBlock> GEYSER_VENT = BLOCKS.registerBlock(
            "geyser_vent",
            GeyserVentBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_WHITE).strength(1.5f));

    private ModBlocks() {}

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
