package com.axes2.geysers.eruption;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.axes2.geysers.Geysers;

import net.minecraft.resources.ResourceLocation;

/**
 * Lookup for {@link GeyserStyle}s by id. M1 keeps a small in-memory table with a single
 * default; M2 replaces the backing store with a hot-reloadable datapack registry while
 * keeping this same {@code get}/{@code exists} surface. Unknown ids fall back to the
 * default so a missing or bad style never crashes an eruption.
 */
public final class GeyserStyles {
    public static final ResourceLocation DEFAULT_ID =
            ResourceLocation.fromNamespaceAndPath(Geysers.MODID, "default");

    private static final Map<ResourceLocation, GeyserStyle> STYLES = new HashMap<>();

    static {
        STYLES.put(DEFAULT_ID, GeyserStyle.defaultStyle());
    }

    private GeyserStyles() {}

    public static GeyserStyle get(ResourceLocation id) {
        return STYLES.getOrDefault(id, GeyserStyle.defaultStyle());
    }

    public static boolean exists(ResourceLocation id) {
        return STYLES.containsKey(id);
    }

    public static Set<ResourceLocation> ids() {
        return STYLES.keySet();
    }
}
