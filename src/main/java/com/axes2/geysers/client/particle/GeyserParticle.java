package com.axes2.geysers.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * A single flexible textured billboard used for every geyser particle kind (steam, spray,
 * mist, bubble). Behaviour — buoyancy/gravity, drag, lifetime, size, tint, opacity — is
 * supplied per kind via {@link Settings}, so all the look tuning lives in one place.
 */
public class GeyserParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    protected GeyserParticle(ClientLevel level, double x, double y, double z,
                             double dx, double dy, double dz, SpriteSet sprites, Settings settings) {
        super(level, x, y, z);
        this.sprites = sprites;
        this.xd = dx;
        this.yd = dy;
        this.zd = dz;
        this.gravity = settings.gravity();
        this.friction = settings.friction();
        this.hasPhysics = settings.hasPhysics();
        this.lifetime = settings.lifetime() + this.random.nextInt(Math.max(1, settings.lifetimeJitter()));
        this.quadSize = settings.size() * (0.75f + this.random.nextFloat() * 0.5f);
        this.alpha = settings.alpha();
        float r = ((settings.tint() >> 16) & 0xFF) / 255f;
        float g = ((settings.tint() >> 8) & 0xFF) / 255f;
        float b = (settings.tint() & 0xFF) / 255f;
        this.setColor(r, g, b);
        this.setSpriteFromAge(sprites);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(sprites);
        // Ease opacity out over the back half of life so particles dissolve rather than pop.
        float lifeFrac = (float) this.age / (float) this.lifetime;
        if (lifeFrac > 0.5f) {
            this.alpha = Math.max(0f, this.alpha * (1f - (lifeFrac - 0.5f) / 0.5f));
        }
    }

    /** Per-kind behaviour bundle. Colors are 0xRRGGBB. */
    public record Settings(float gravity, float friction, boolean hasPhysics,
                           int lifetime, int lifetimeJitter, float size, int tint, float alpha) {}

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        private final Settings settings;

        public Provider(SpriteSet sprites, Settings settings) {
            this.sprites = sprites;
            this.settings = settings;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z, double dx, double dy, double dz) {
            return new GeyserParticle(level, x, y, z, dx, dy, dz, sprites, settings);
        }
    }
}
