package org.elemental_reaction.event;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import org.elemental_reaction.Elemental_reaction;

public final class BurningParticles {
    private BurningParticles() {
    }

    public static void spawn(ServerLevel level, LivingEntity target,
                             String attachedElement, String incomingElement) {
        RandomSource random = level.getRandom();
        double centerX = target.getX();
        double centerY = target.getY();
        double centerZ = target.getZ();
        double height = Math.max(0.8D, target.getBbHeight());
        double radius = Math.max(0.24D, target.getBbWidth() * 0.48D);

        for (int i = 14; i < 14 + 10; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double distance = radius * (0.45D + random.nextDouble() * 0.7D);
            send(level, Elemental_reaction.BURNING_FLAME_PARTICLE.get(),
                    centerX + Math.cos(angle) * distance,
                    centerY + 0.08D + random.nextDouble() * height * 0.82D,
                    centerZ + Math.sin(angle) * distance,
                    (random.nextDouble() - 0.5D) * 0.012D,
                    0.026D + random.nextDouble() * 0.032D,
                    (random.nextDouble() - 0.5D) * 0.012D);
        }

        for (int i = 0; i < 12; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double distance = random.nextDouble() * radius;
            send(level, Elemental_reaction.BURNING_GRASS_BIT_PARTICLE.get(),
                    centerX + Math.cos(angle) * distance,
                    centerY + 0.15D + random.nextDouble() * height * 0.72D,
                    centerZ + Math.sin(angle) * distance,
                    Math.cos(angle) * (0.018D + random.nextDouble() * 0.024D),
                    0.022D + random.nextDouble() * 0.028D,
                    Math.sin(angle) * (0.018D + random.nextDouble() * 0.024D));
        }

        for (int i = 0; i < 14; i++) {
            scatter(level, target, Elemental_reaction.BURNING_SPARK_PARTICLE.get(),
                    0.035D + random.nextDouble() * 0.035D);
        }

        for (int i = 0; i < 10; i++) {
            scatter(level, target, Elemental_reaction.BURNING_EMBER_PARTICLE.get(),
                    0.018D + random.nextDouble() * 0.022D);
        }

        for (int i = 0; i < 4; i++) {
            scatter(level, target, Elemental_reaction.BURNING_BLACK_SMOKE_PARTICLE.get(), 0.008D);
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
                0.012D + random.nextDouble() * speed,
                Math.sin(angle) * speed);
    }

    private static void send(ServerLevel level, SimpleParticleType particle,
                             double x, double y, double z,
                             double vx, double vy, double vz) {
        level.sendParticles(particle, x, y, z, 0, vx, vy, vz, 0.0D);
    }
}
