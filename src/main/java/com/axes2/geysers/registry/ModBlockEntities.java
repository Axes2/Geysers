package com.axes2.geysers.registry;

import com.axes2.geysers.Geysers;
import com.axes2.geysers.block.entity.GeyserVentBlockEntity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Block-entity-type registry. The vent block entity owns the eruption state machine.
 */
public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Geysers.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GeyserVentBlockEntity>> GEYSER_VENT =
            BLOCK_ENTITY_TYPES.register("geyser_vent",
                    () -> BlockEntityType.Builder.of(GeyserVentBlockEntity::new, ModBlocks.GEYSER_VENT.get()).build(null));

    private ModBlockEntities() {}

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITY_TYPES.register(modEventBus);
    }
}
