package org.elemental_reaction.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.particles.SimpleParticleType;

public final class BurningParticle extends TextureSheetParticle {
    public enum Kind {
        FLAME,
        EMBER,
        SPARK,
        GRASS_BIT,
        SMOKE
    }

    private final SpriteSet sprites;
    private final Kind kind;
    private final float baseSize;
    private final float baseAlpha;

    private BurningParticle(ClientLevel level, double x, double y, double z,
                            double vx, double vy, double vz,
                            SpriteSet sprites, Kind kind) {
        super(level, x, y, z);
        this.sprites = sprites;
        this.kind = kind;
        xd = vx;
        yd = vy;
        zd = vz;
        hasPhysics = false;
        gravity = 0.0F;
        friction = 0.94F;

        float alpha = 0.92F;
        switch (kind) {
            case FLAME -> {
                quadSize = 0.2F + random.nextFloat() * 0.12F;
                lifetime = 9 + random.nextInt(6);
                friction = 0.97F;
            }
            case EMBER -> {
                quadSize = 0.075F + random.nextFloat() * 0.055F;
                lifetime = 13 + random.nextInt(9);
                gravity = 0.012F;
                friction = 0.95F;
            }
            case SPARK -> {
                quadSize = 0.07F + random.nextFloat() * 0.05F;
                lifetime = 8 + random.nextInt(6);
                friction = 0.96F;
            }
            case GRASS_BIT -> {
                quadSize = 0.095F + random.nextFloat() * 0.07F;
                lifetime = 14 + random.nextInt(10);
                gravity = 0.018F;
                friction = 0.92F;
                roll = random.nextFloat() * 6.2831855F;
                oRoll = roll;
            }
            case SMOKE -> {
                alpha = 0.66F;
                quadSize = 0.18F + random.nextFloat() * 0.12F;
                lifetime = 20 + random.nextInt(13);
                gravity = -0.001F;
                friction = 0.975F;
            }
        }

        baseSize = quadSize;
        baseAlpha = alpha;
        setAlpha(alpha);
        setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        if (removed) {
            return;
        }

        float progress = (float) age / (float) lifetime;
        setSpriteFromAge(sprites);

        float fade = progress < 0.5F
                ? 1.0F
                : 1.0F - (progress - 0.5F) / 0.5F;
        setAlpha(baseAlpha * Math.max(0.0F, fade));

        if (kind == Kind.FLAME) {
            quadSize = baseSize * (1.0F - progress * 0.2F);
        } else if (kind == Kind.SMOKE) {
            quadSize = baseSize * (1.0F + progress * 0.5F);
        } else if (kind == Kind.GRASS_BIT) {
            roll += 0.16F;
        }
    }

    @Override
    protected int getLightColor(float partialTick) {
        return kind == Kind.SMOKE ? super.getLightColor(partialTick) : LightTexture.FULL_BRIGHT;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        private final Kind kind;

        public Provider(SpriteSet sprites, Kind kind) {
            this.sprites = sprites;
            this.kind = kind;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double vx, double vy, double vz) {
            return new BurningParticle(level, x, y, z, vx, vy, vz, sprites, kind);
        }
    }
}
