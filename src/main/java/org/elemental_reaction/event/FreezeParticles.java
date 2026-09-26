package org.elemental_reaction.event;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import org.elemental_reaction.Elemental_reaction;

public final class FreezeParticles {
    private FreezeParticles() {
    }

    public static void spawn(ServerLevel level, LivingEntity target,
                             String attachedElement, String incomingElement) {
        RandomSource random = level.getRandom();
        double centerX = target.getX();
        double centerY = target.getY();
        double centerZ = target.getZ();
        double height = Math.max(0.8D, target.getBbHeight());
        double radius = Math.max(0.28D, target.getBbWidth() * 0.58D);

        send(level, Elemental_reaction.FREEZE_ICE_RING_PARTICLE.get(),
                centerX, centerY + 0.025D, centerZ, radius * 2.1D, 0.0D, 0.0D);

        for (int i = 0; i < 16; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double distance = radius * (0.35D + random.nextDouble() * 0.9D);
            send(level, Elemental_reaction.FREEZE_ICE_CRYSTAL_PARTICLE.get(),
                    centerX + Math.cos(angle) * distance,
                    centerY + 0.12D + random.nextDouble() * height * 0.86D,
                    centerZ + Math.sin(angle) * distance,
                    Math.cos(angle) * (0.012D + random.nextDouble() * 0.022D),
                    0.018D + random.nextDouble() * 0.026D,
                    Math.sin(angle) * (0.012D + random.nextDouble() * 0.022D));
        }

        for (int i = 0; i < 12; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double distance = radius * (0.25D + random.nextDouble() * 0.75D);
            send(level, Elemental_reaction.FREEZE_ICE_SHARD_PARTICLE.get(),
                    centerX + Math.cos(angle) * distance,
                    centerY + 0.16D + random.nextDouble() * height * 0.72D,
                    centerZ + Math.sin(angle) * distance,
                    Math.cos(angle) * (0.02D + random.nextDouble() * 0.035D),
                    0.025D + random.nextDouble() * 0.03D,
                    Math.sin(angle) * (0.02D + random.nextDouble() * 0.035D));
        }

        for (int i = 0; i < 10; i++) {
            spawnMist(level, target, radius, height);
        }

        for (int i = 0; i < 12; i++) {
            spawnGlint(level, target, radius, height);
        }
    }

    public static void tick(ServerLevel level, LivingEntity target) {
        if (level.getGameTime() % 4L != 0L) {
            return;
        }

        double radius = Math.max(0.28D, target.getBbWidth() * 0.58D);
        double height = Math.max(0.8D, target.getBbHeight());
        spawnMist(level, target, radius, height);
        spawnMist(level, target, radius, height);
        spawnGlint(level, target, radius, height);
    }

    private static void spawnMist(ServerLevel level, LivingEntity target, double radius, double height) {
        RandomSource random = level.getRandom();
        double angle = random.nextDouble() * Math.PI * 2.0D;
        double distance = radius * (0.35D + random.nextDouble() * 0.7D);
        send(level, Elemental_reaction.FREEZE_FROST_MIST_PARTICLE.get(),
                target.getX() + Math.cos(angle) * distance,
                target.getY() + 0.12D + random.nextDouble() * height * 0.78D,
                target.getZ() + Math.sin(angle) * distance,
                (random.nextDouble() - 0.5D) * 0.004D,
                0.004D + random.nextDouble() * 0.008D,
                (random.nextDouble() - 0.5D) * 0.004D);
    }

    private static void spawnGlint(ServerLevel level, LivingEntity target, double radius, double height) {
        RandomSource random = level.getRandom();
        double angle = random.nextDouble() * Math.PI * 2.0D;
        double distance = radius * (0.45D + random.nextDouble() * 0.65D);
        send(level, Elemental_reaction.FREEZE_SNOW_GLINT_PARTICLE.get(),
                target.getX() + Math.cos(angle) * distance,
                target.getY() + 0.2D + random.nextDouble() * height * 0.82D,
                target.getZ() + Math.sin(angle) * distance,
                (random.nextDouble() - 0.5D) * 0.006D,
                0.008D + random.nextDouble() * 0.012D,
                (random.nextDouble() - 0.5D) * 0.006D);
    }

    private static void send(ServerLevel level, SimpleParticleType particle,
                             double x, double y, double z,
                             double vx, double vy, double vz) {
        level.sendParticles(particle, x, y, z, 0, vx, vy, vz, 0.0D);
    }
}
