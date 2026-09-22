package org.elemental_reaction.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public class ElementAttachmentParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    private ElementAttachmentParticle(ClientLevel level, double x, double y, double z,
                                      double red, double green, double blue, SpriteSet sprites) {
        super(level, x, y, z);
        this.sprites = sprites;
        setColor((float) red, (float) green, (float) blue);
        setAlpha(0.82F);
        setSpriteFromAge(sprites);

        quadSize = 0.065F + random.nextFloat() * 0.055F;
        lifetime = 10 + random.nextInt(13);
        gravity = 0.055F;
        yd = -0.004D - random.nextDouble() * 0.014D;
        xd = (random.nextDouble() - 0.5D) * 0.008D;
        zd = (random.nextDouble() - 0.5D) * 0.008D;
        hasPhysics = false;
    }

    @Override
    public void tick() {
        super.tick();
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
            return new ElementAttachmentParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
        }
    }
}
