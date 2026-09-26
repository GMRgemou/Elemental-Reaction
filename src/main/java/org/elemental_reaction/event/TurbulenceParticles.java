package org.elemental_reaction.event;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import org.elemental_reaction.Elemental_reaction;

public final class TurbulenceParticles {
    private TurbulenceParticles() {
    }

    public static void spawn(ServerLevel level, LivingEntity target,
                             String attachedElement, String incomingElement) {
        RandomSource random = level.getRandom();
        double centerX = target.getX();
        double centerY = target.getY();
        double centerZ = target.getZ();
        double height = Math.max(0.8D, target.getBbHeight());
        double radius = Math.max(0.3D, target.getBbWidth() * 0.58D);

        for (int i = 0; i < 20; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double distance = radius * (0.75D + random.nextDouble() * 0.8D);
            double y = centerY + 0.08D + random.nextDouble() * Math.min(0.42D, height * 0.35D);
            double tangentX = -Math.sin(angle);
            double tangentZ = Math.cos(angle);
            double speed = 0.012D + random.nextDouble() * 0.018D;

            send(level, Elemental_reaction.TURBULENCE_ARC_PARTICLE.get(),
                    centerX + Math.cos(angle) * distance, y,
                    centerZ + Math.sin(angle) * distance,
                    tangentX * speed, 0.008D + random.nextDouble() * 0.01D, tangentZ * speed);
        }

        for (int i = 0; i < 16; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double distance = random.nextDouble() * radius;
            double y = centerY + 0.1D + random.nextDouble() * height * 0.72D;
            double tangentX = -Math.sin(angle);
            double tangentZ = Math.cos(angle);

            send(level, Elemental_reaction.TURBULENCE_TIDE_DROP_PARTICLE.get(),
                    centerX + Math.cos(angle) * distance, y,
                    centerZ + Math.sin(angle) * distance,
                    tangentX * 0.01D, 0.028D + random.nextDouble() * 0.035D,
                    tangentZ * 0.01D);
        }

        for (int i = 0; i < 12; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double distance = 0.12D + random.nextDouble() * radius;
            send(level, Elemental_reaction.TURBULENCE_WATER_DROP_PARTICLE.get(),
                    centerX + Math.cos(angle) * distance,
                    centerY + 0.25D + random.nextDouble() * height * 0.65D,
                    centerZ + Math.sin(angle) * distance,
                    (random.nextDouble() - 0.5D) * 0.018D,
                    0.015D + random.nextDouble() * 0.026D,
                    (random.nextDouble() - 0.5D) * 0.018D);
        }

        for (int i = 0; i < 9; i++) {
            scatter(level, target, Elemental_reaction.TURBULENCE_DARK_SPARK_PARTICLE.get(), 0.045D);
        }

        for (int i = 0; i < 5; i++) {
            scatter(level, target, Elemental_reaction.TURBULENCE_BLACK_SMOKE_PARTICLE.get(), 0.012D);
        }
    }

    public static void tick(ServerLevel level, LivingEntity target) {
        long phase = (level.getGameTime() + target.getId()) % 9L;
        if (phase % 3L != 0L) {
            return;
        }

        RandomSource random = level.getRandom();
        double angle = random.nextDouble() * Math.PI * 2.0D;
        double radius = Math.max(0.3D, target.getBbWidth() * 0.65D);
        double x = target.getX() + Math.cos(angle) * radius;
        double y = target.getY() + 0.1D + random.nextDouble() * target.getBbHeight() * 0.85D;
        double z = target.getZ() + Math.sin(angle) * radius;
        double direction = phase == 0L ? 1.0D : -1.0D;

        send(level, Elemental_reaction.TURBULENCE_ARC_PARTICLE.get(),
                x, y, z, -Math.sin(angle) * 0.018D * direction, 0.008D,
                Math.cos(angle) * 0.018D * direction);
        send(level, Elemental_reaction.TURBULENCE_TIDE_DROP_PARTICLE.get(),
                x, y - 0.08D, z, -Math.sin(angle) * 0.008D,
                0.025D, Math.cos(angle) * 0.008D);

        if (phase == 0L) {
            scatter(level, target, Elemental_reaction.TURBULENCE_DARK_SPARK_PARTICLE.get(), 0.035D);
            send(level, Elemental_reaction.TURBULENCE_BLACK_SMOKE_PARTICLE.get(),
                    x, y, z, 0.0D, 0.012D, 0.0D);
        } else if (phase == 6L) {
            scatter(level, target, Elemental_reaction.TURBULENCE_WATER_DROP_PARTICLE.get(), 0.018D);
        }
    }

    private static void scatter(ServerLevel level, LivingEntity target,
                                SimpleParticleType particle, double speed) {
        RandomSource random = level.getRandom();
        double angle = random.nextDouble() * Math.PI * 2.0D;
        double radius = random.nextDouble() * Math.max(0.2D, target.getBbWidth() * 0.55D);
        send(level, particle,
                target.getX() + Math.cos(angle) * radius,
                target.getY() + 0.12D + random.nextDouble() * target.getBbHeight() * 0.82D,
                target.getZ() + Math.sin(angle) * radius,
                Math.cos(angle) * speed,
                0.008D + random.nextDouble() * speed,
                Math.sin(angle) * speed);
    }

    private static void send(ServerLevel level, SimpleParticleType particle,
                             double x, double y, double z,
                             double vx, double vy, double vz) {
        level.sendParticles(particle, x, y, z, 0, vx, vy, vz, 0.0D);
    }
}
