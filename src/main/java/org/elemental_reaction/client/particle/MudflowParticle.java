package org.elemental_reaction.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public final class MudflowParticle extends TextureSheetParticle {
    private enum Kind {
        MUD_BLOB,
        MUD_CHUNK,
        SWAMP_MOTE,
        WATER_DROP,
        MUD_RING
    }

    private final SpriteSet sprites;
    private final Kind kind;
    private final float baseQuadSize;
    private final float baseAlpha;

    private MudflowParticle(ClientLevel level, double x, double y, double z,
                            double xSpeed, double ySpeed, double zSpeed,
                            SpriteSet sprites, Kind kind) {
        super(level, x, y, z);
        this.sprites = sprites;
        this.kind = kind;
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;
        this.hasPhysics = false;

        float configuredAlpha = 1.0F;
        switch (kind) {
            case MUD_BLOB -> {
                configuredAlpha = 0.94F;
                setAlpha(configuredAlpha);
                quadSize = 0.11F + random.nextFloat() * 0.08F;
                lifetime = 14 + random.nextInt(10);
                gravity = 0.045F;
                friction = 0.91F;
            }
            case MUD_CHUNK -> {
                configuredAlpha = 0.98F;
                setAlpha(configuredAlpha);
                quadSize = 0.075F + random.nextFloat() * 0.055F;
                lifetime = 16 + random.nextInt(11);
                gravity = 0.065F;
                friction = 0.9F;
                roll = random.nextFloat() * 6.2831855F;
                oRoll = roll;
            }
            case SWAMP_MOTE -> {
                configuredAlpha = 0.75F;
                setAlpha(configuredAlpha);
                quadSize = 0.035F + random.nextFloat() * 0.025F;
                lifetime = 20 + random.nextInt(16);
                gravity = -0.0015F;
                friction = 0.98F;
            }
            case WATER_DROP -> {
                configuredAlpha = 0.88F;
                setAlpha(configuredAlpha);
                quadSize = 0.055F + random.nextFloat() * 0.035F;
                lifetime = 11 + random.nextInt(7);
                gravity = 0.055F;
                friction = 0.94F;
            }
            case MUD_RING -> {
                configuredAlpha = 0.72F;
                setAlpha(configuredAlpha);
                quadSize = xSpeed > 0.0D ? (float) xSpeed : 0.8F;
                lifetime = 12;
                gravity = 0.0F;
                friction = 1.0F;
                this.xd = 0.0D;
                this.yd = 0.0D;
                this.zd = 0.0D;
            }
        }

        baseQuadSize = quadSize;
        baseAlpha = configuredAlpha;
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

        if (kind == Kind.MUD_RING) {
            quadSize = baseQuadSize * (0.72F + progress * 0.78F);
            setAlpha(0.72F * (1.0F - progress));
            return;
        }

        if (progress > 0.58F) {
            setAlpha(baseAlpha * (1.0F - (progress - 0.58F) / 0.42F));
        }

        if (kind == Kind.SWAMP_MOTE) {
            quadSize = baseQuadSize * (1.0F + 0.18F * (float) Math.sin(age * 0.55D));
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static final class MudBlobProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public MudBlobProvider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            return new MudflowParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites, Kind.MUD_BLOB);
        }
    }

    public static final class MudChunkProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public MudChunkProvider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            return new MudflowParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites, Kind.MUD_CHUNK);
        }
    }

    public static final class SwampMoteProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public SwampMoteProvider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            return new MudflowParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites, Kind.SWAMP_MOTE);
        }
    }

    public static final class WaterDropProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public WaterDropProvider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            return new MudflowParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites, Kind.WATER_DROP);
        }
    }

    public static final class MudRingProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public MudRingProvider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            return new MudflowParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites, Kind.MUD_RING);
        }
    }
}
