package org.elemental_reaction.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public class ElementDiffusionParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final float baseQuadSize;

    private ElementDiffusionParticle(ClientLevel level, double x, double y, double z,
                                     double red, double green, double blue, SpriteSet sprites) {
        super(level, x, y, z);
        this.sprites = sprites;
        setColor((float) red, (float) green, (float) blue);
        setAlpha(0.9F);

        quadSize = 0.36F + random.nextFloat() * 0.18F;
        baseQuadSize = quadSize;
        lifetime = 12 + random.nextInt(5);
        gravity = 0.0F;
        xd = 0.0D;
        yd = 0.0D;
        zd = 0.0D;
        roll = 0.0F;
        oRoll = roll;
        hasPhysics = false;
        setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        if (removed) {
            return;
        }

        float progress = (float) age / (float) lifetime;
        setAlpha(0.9F * (1.0F - progress));
        quadSize = baseQuadSize * (1.0F + progress * 0.28F);
        setSpriteFromAge(sprites);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            return new ElementDiffusionParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
        }
    }
}
