package org.elemental_reaction.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.particles.SimpleParticleType;

public final class SoulScorchParticle extends TextureSheetParticle {
    public enum Kind {
        FLAME, DARK_SPARK, SMOKE, SHARD, SOUL_SPARK, RING
    }

    private final SpriteSet sprites;
    private final Kind kind;
    private final float baseSize;
    private final float baseAlpha;

    private SoulScorchParticle(ClientLevel level, double x, double y, double z,
                               double vx, double vy, double vz, SpriteSet sprites, Kind kind) {
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
            case FLAME -> {
                quadSize = 0.25F + random.nextFloat() * 0.16F;
                lifetime = 16 + random.nextInt(12);
            }
            case DARK_SPARK -> {
                quadSize = 0.1F + random.nextFloat() * 0.06F;
                lifetime = 9 + random.nextInt(7);
            }
            case SMOKE -> {
                alpha = 0.75F;
                quadSize = 0.22F + random.nextFloat() * 0.14F;
                lifetime = 23 + random.nextInt(14);
                friction = 0.97F;
            }
            case SHARD -> {
                quadSize = 0.14F + random.nextFloat() * 0.07F;
                lifetime = 17 + random.nextInt(12);
            }
            case SOUL_SPARK -> {
                quadSize = 0.14F + random.nextFloat() * 0.08F;
                lifetime = 9 + random.nextInt(8);
            }
            case RING -> {
                alpha = 0.7F;
                quadSize = 0.85F;
                lifetime = 13;
                xd = 0.0D;
                yd = 0.0D;
                zd = 0.0D;
            }
        }
        baseAlpha = alpha;
        baseSize = quadSize;
        setAlpha(alpha);
        setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        if (removed) {
            return;
        }
        float progress = (float) age / lifetime;
        setAlpha(baseAlpha * Math.min(1.0F, (1.0F - progress) * 2.0F));
        if (kind == Kind.RING) {
            quadSize = baseSize * (0.6F + progress * 0.85F);
        } else if (kind == Kind.SMOKE) {
            quadSize = baseSize * (1.0F + progress * 0.4F);
        }
        setSpriteFromAge(sprites);
    }

    @Override
    protected int getLightColor(float partialTick) {
        return kind == Kind.SMOKE || kind == Kind.RING
                ? super.getLightColor(partialTick) : LightTexture.FULL_BRIGHT;
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
            return new SoulScorchParticle(level, x, y, z, vx, vy, vz, sprites, kind);
        }
    }
}
