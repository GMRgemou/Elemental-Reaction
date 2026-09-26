package org.elemental_reaction.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import org.elemental_reaction.Elemental_reaction;

public final class MudflowParticles {
    private static final int CONTINUOUS_BLOB_COUNT = 2;
    private static final int DENSE_TICK_INTERVAL = 4;

    private MudflowParticles() {
    }

    public static void spawn(ServerLevel level, LivingEntity target,
                             String attachedElement, String incomingElement) {
        RandomSource random = level.getRandom();
        double centerX = target.getX();
        double centerY = target.getY() + 0.06D;
        double centerZ = target.getZ();

        send(level, Elemental_reaction.MUD_RING_PARTICLE.get(),
                centerX, centerY, centerZ, 0.95D, 0.0D, 0.0D);

        for (int index = 0; index < 24; index++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double radius = 0.06D + random.nextDouble() * 0.42D;
            double x = centerX + Math.cos(angle) * radius;
            double z = centerZ + Math.sin(angle) * radius;
            send(level, Elemental_reaction.MUD_BLOB_PARTICLE.get(),
                    x, centerY + random.nextDouble() * 0.12D, z,
                    Math.cos(angle) * 0.035D, 0.07D + random.nextDouble() * 0.11D,
                    Math.sin(angle) * 0.035D);
        }

        for (int index = 0; index < 12; index++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double radius = 0.1D + random.nextDouble() * 0.38D;
            send(level, Elemental_reaction.MUD_CHUNK_PARTICLE.get(),
                    centerX + Math.cos(angle) * radius,
                    centerY + 0.04D + random.nextDouble() * 0.12D,
                    centerZ + Math.sin(angle) * radius,
                    Math.cos(angle) * 0.06D, 0.08D + random.nextDouble() * 0.1D,
                    Math.sin(angle) * 0.06D);
        }

        for (int index = 0; index < 12; index++) {
            send(level, Elemental_reaction.WATER_DROP_PARTICLE.get(),
                    centerX + (random.nextDouble() - 0.5D) * 0.5D,
                    centerY + 0.16D + random.nextDouble() * 0.42D,
                    centerZ + (random.nextDouble() - 0.5D) * 0.5D,
                    (random.nextDouble() - 0.5D) * 0.025D,
                    0.02D + random.nextDouble() * 0.08D,
                    (random.nextDouble() - 0.5D) * 0.025D);
        }

        for (int index = 0; index < 16; index++) {
            send(level, Elemental_reaction.SWAMP_MOTE_PARTICLE.get(),
                    centerX + (random.nextDouble() - 0.5D) * 0.65D,
                    centerY + 0.2D + random.nextDouble() * Math.max(0.4D, target.getBbHeight() * 0.65D),
                    centerZ + (random.nextDouble() - 0.5D) * 0.65D,
                    (random.nextDouble() - 0.5D) * 0.008D,
                    0.004D + random.nextDouble() * 0.014D,
                    (random.nextDouble() - 0.5D) * 0.008D);
        }
    }

    public static void tick(ServerLevel level, LivingEntity target) {
        RandomSource random = level.getRandom();
        double centerX = target.getX();
        double centerY = target.getY() + 0.14D;
        double centerZ = target.getZ();

        // Keep a small stream alive every tick so the effect does not visibly
        // disappear between the larger periodic bursts.
        for (int index = 0; index < CONTINUOUS_BLOB_COUNT; index++) {
            send(level, Elemental_reaction.MUD_BLOB_PARTICLE.get(),
                    centerX + (random.nextDouble() - 0.5D) * 0.58D,
                    centerY + random.nextDouble() * Math.max(0.25D, target.getBbHeight() * 0.55D),
                    centerZ + (random.nextDouble() - 0.5D) * 0.58D,
                    (random.nextDouble() - 0.5D) * 0.024D,
                    0.018D + random.nextDouble() * 0.042D,
                    (random.nextDouble() - 0.5D) * 0.024D);
        }

        if ((level.getGameTime() + target.getId()) % DENSE_TICK_INTERVAL == 0L) {
            for (int index = 0; index < 3; index++) {
                double angle = random.nextDouble() * Math.PI * 2.0D;
                double radius = 0.18D + random.nextDouble() * 0.42D;
                send(level, Elemental_reaction.MUD_BLOB_PARTICLE.get(),
                        centerX + Math.cos(angle) * radius,
                        centerY + random.nextDouble() * Math.max(0.3D, target.getBbHeight() * 0.55D),
                        centerZ + Math.sin(angle) * radius,
                        Math.cos(angle) * 0.028D,
                        0.025D + random.nextDouble() * 0.04D,
                        Math.sin(angle) * 0.028D);
            }

            send(level, Elemental_reaction.MUD_CHUNK_PARTICLE.get(),
                    centerX + (random.nextDouble() - 0.5D) * 0.62D,
                    centerY + random.nextDouble() * Math.max(0.25D, target.getBbHeight() * 0.45D),
                    centerZ + (random.nextDouble() - 0.5D) * 0.62D,
                    (random.nextDouble() - 0.5D) * 0.035D,
                    0.035D + random.nextDouble() * 0.045D,
                    (random.nextDouble() - 0.5D) * 0.035D);

            send(level, Elemental_reaction.SWAMP_MOTE_PARTICLE.get(),
                    centerX + (random.nextDouble() - 0.5D) * 0.6D,
                    centerY + random.nextDouble() * Math.max(0.35D, target.getBbHeight() * 0.7D),
                    centerZ + (random.nextDouble() - 0.5D) * 0.6D,
                    (random.nextDouble() - 0.5D) * 0.008D,
                    0.006D + random.nextDouble() * 0.014D,
                    (random.nextDouble() - 0.5D) * 0.008D);
        }
    }

    public static void damageTick(ServerLevel level, LivingEntity target) {
        RandomSource random = level.getRandom();
        double centerX = target.getX();
        double centerY = target.getY() + 0.08D;
        double centerZ = target.getZ();

        send(level, Elemental_reaction.MUD_RING_PARTICLE.get(),
                centerX, centerY, centerZ, 0.72D, 0.0D, 0.0D);

        for (int index = 0; index < 12; index++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double radius = 0.08D + random.nextDouble() * 0.34D;
            send(level, Elemental_reaction.MUD_BLOB_PARTICLE.get(),
                    centerX + Math.cos(angle) * radius,
                    centerY + random.nextDouble() * 0.18D,
                    centerZ + Math.sin(angle) * radius,
                    Math.cos(angle) * 0.045D, 0.08D + random.nextDouble() * 0.08D,
                    Math.sin(angle) * 0.045D);
        }

        for (int index = 0; index < 7; index++) {
            send(level, Elemental_reaction.WATER_DROP_PARTICLE.get(),
                    centerX + (random.nextDouble() - 0.5D) * 0.45D,
                    centerY + 0.18D + random.nextDouble() * 0.22D,
                    centerZ + (random.nextDouble() - 0.5D) * 0.45D,
                    (random.nextDouble() - 0.5D) * 0.03D,
                    0.04D + random.nextDouble() * 0.08D,
                    (random.nextDouble() - 0.5D) * 0.03D);
        }
    }

    private static void send(ServerLevel level, net.minecraft.core.particles.SimpleParticleType particle,
                             double x, double y, double z,
                             double xSpeed, double ySpeed, double zSpeed) {
        level.sendParticles(particle, x, y, z, 0, xSpeed, ySpeed, zSpeed, 0.0D);
    }
}
