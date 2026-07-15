package com.axes2.geysers.registry;

import com.axes2.geysers.Geysers;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Creative tab holding the mod's items (currently just the vent).
 */
public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Geysers.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> GEYSERS_TAB = TABS.register(
            "geysers",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.geysers"))
                    .icon(() -> ModItems.GEYSER_VENT_ITEM.get().getDefaultInstance())
                    .displayItems((parameters, output) -> output.accept(ModItems.GEYSER_VENT_ITEM.get()))
                    .build());

    private ModCreativeTabs() {}

    public static void register(IEventBus modEventBus) {
        TABS.register(modEventBus);
    }
}
