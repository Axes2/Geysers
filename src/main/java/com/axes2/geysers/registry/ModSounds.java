package com.axes2.geysers.registry;

import com.axes2.geysers.Geysers;

import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Sound-event registry. Priming rumble, surge crack, sustained roar, blue-bubble pop,
 * violent steam roar and the drain tail are added in M5.
 */
public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, Geysers.MODID);

    private ModSounds() {}

    public static void register(IEventBus modEventBus) {
        SOUND_EVENTS.register(modEventBus);
    }
}
