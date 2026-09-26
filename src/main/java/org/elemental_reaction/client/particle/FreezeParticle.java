package org.elemental_reaction.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.particles.SimpleParticleType;

public final class FreezeParticle extends TextureSheetParticle {
    public enum Kind {
        ICE_RING,
        ICE_CRYSTAL,
        FROST_MIST,
        ICE_SHARD,
        SNOW_GLINT
    }

    private final SpriteSet sprites;
    private final Kind kind;
    private final float baseSize;
    private final float baseAlpha;

    private FreezeParticle(ClientLevel level, double x, double y, double z,
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
        friction = 0.95F;

        float alpha = 0.9F;
        switch (kind) {
            case ICE_RING -> {
                quadSize = (float) Math.max(0.45D, vx);
                lifetime = 14;
                alpha = 0.78F;
                friction = 1.0F;
                xd = 0.0D;
                yd = 0.0D;
                zd = 0.0D;
            }
            case ICE_CRYSTAL -> {
                quadSize = 0.11F + random.nextFloat() * 0.08F;
                lifetime = 24 + random.nextInt(15);
                gravity = -0.001F;
            }
            case FROST_MIST -> {
                quadSize = 0.18F + random.nextFloat() * 0.13F;
                lifetime = 28 + random.nextInt(18);
                alpha = 0.38F;
                gravity = -0.0015F;
                friction = 0.98F;
            }
            case ICE_SHARD -> {
                quadSize = 0.09F + random.nextFloat() * 0.065F;
                lifetime = 18 + random.nextInt(12);
                gravity = 0.012F;
                roll = random.nextFloat() * 6.2831855F;
                oRoll = roll;
            }
            case SNOW_GLINT -> {
                quadSize = 0.07F + random.nextFloat() * 0.055F;
                lifetime = 20 + random.nextInt(18);
                alpha = 0.92F;
                gravity = -0.002F;
                friction = 0.98F;
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

        switch (kind) {
            case ICE_RING -> quadSize = baseSize * (0.72F + progress * 0.85F);
            case FROST_MIST -> quadSize = baseSize * (1.0F + progress * 0.5F);
            case ICE_CRYSTAL, SNOW_GLINT -> quadSize =
                    baseSize * (0.85F + 0.2F * (float) Math.sin(age * 0.45D));
            case ICE_SHARD -> roll += 0.14F;
        }
    }

    @Override
    protected int getLightColor(float partialTick) {
        return kind == Kind.FROST_MIST ? super.getLightColor(partialTick) : LightTexture.FULL_BRIGHT;
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
            return new FreezeParticle(level, x, y, z, vx, vy, vz, sprites, kind);
        }
    }
}
