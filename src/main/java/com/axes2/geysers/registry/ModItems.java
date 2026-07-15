package com.axes2.geysers.registry;

import com.axes2.geysers.Geysers;

import net.minecraft.world.item.BlockItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Item registry. The debug "Geyser Wand" is added in M8.
 */
public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Geysers.MODID);

    /** BlockItem for placing the vent in creative. */
    public static final DeferredItem<BlockItem> GEYSER_VENT_ITEM =
            ITEMS.registerSimpleBlockItem("geyser_vent", ModBlocks.GEYSER_VENT);

    private ModItems() {}

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
