package com.axes2.geysers.registry;

import com.axes2.geysers.Geysers;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Item registry. The vent block item and the debug "Geyser Wand" are added in M1/M8.
 */
public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Geysers.MODID);

    private ModItems() {}

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
