package org.elemental_reaction.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.particles.SimpleParticleType;

public final class TurbulenceParticle extends TextureSheetParticle {
    public enum Kind {
        ARC,
        TIDE_DROP,
        WATER_DROP,
        DARK_SPARK,
        SMOKE
    }

    private final SpriteSet sprites;
    private final Kind kind;
    private final float baseSize;
    private final float baseAlpha;

    private TurbulenceParticle(ClientLevel level, double x, double y, double z,
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

        float alpha = 0.9F;
        switch (kind) {
            case ARC -> {
                quadSize = 0.18F + random.nextFloat() * 0.12F;
                lifetime = 9 + random.nextInt(6);
                friction = 0.97F;
                roll = random.nextFloat() * 6.2831855F;
                oRoll = roll;
            }
            case TIDE_DROP -> {
                quadSize = 0.095F + random.nextFloat() * 0.065F;
                lifetime = 15 + random.nextInt(9);
                friction = 0.97F;
                gravity = -0.002F;
            }
            case WATER_DROP -> {
                quadSize = 0.07F + random.nextFloat() * 0.045F;
                lifetime = 11 + random.nextInt(7);
                friction = 0.95F;
                gravity = 0.012F;
            }
            case DARK_SPARK -> {
                quadSize = 0.07F + random.nextFloat() * 0.055F;
                lifetime = 8 + random.nextInt(6);
                friction = 0.96F;
            }
            case SMOKE -> {
                alpha = 0.68F;
                quadSize = 0.18F + random.nextFloat() * 0.12F;
                lifetime = 20 + random.nextInt(12);
                friction = 0.975F;
                gravity = -0.001F;
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

        float fade = progress < 0.55F
                ? 1.0F
                : 1.0F - (progress - 0.55F) / 0.45F;
        setAlpha(baseAlpha * Math.max(0.0F, fade));

        if (kind == Kind.ARC) {
            quadSize = baseSize * (0.72F + progress * 0.78F);
            roll += 0.1F;
        } else if (kind == Kind.TIDE_DROP) {
            quadSize = baseSize * (1.0F + progress * 0.2F);
        } else if (kind == Kind.SMOKE) {
            quadSize = baseSize * (1.0F + progress * 0.45F);
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
            return new TurbulenceParticle(level, x, y, z, vx, vy, vz, sprites, kind);
        }
    }
}
