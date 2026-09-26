package org.elemental_reaction.event;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import org.elemental_reaction.Elemental_reaction;

public final class SoulScorchParticles {
    private SoulScorchParticles() {
    }

    public static void spawn(ServerLevel level, LivingEntity target,
                             String attachedElement, String incomingElement) {
        RandomSource random = level.getRandom();
        double x = target.getX();
        double y = target.getY() + target.getBbHeight() * 0.48D;
        double z = target.getZ();
        send(level, Elemental_reaction.SOUL_RING_PARTICLE.get(), x, y, z, 0.0D, 0.0D, 0.0D);

        for (int i = 0; i < 18; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double radius = target.getBbWidth() * 0.35D + random.nextDouble() * 0.28D;
            send(level, Elemental_reaction.SOUL_FLAME_PARTICLE.get(),
                    x + Math.cos(angle) * radius,
                    target.getY() + random.nextDouble() * target.getBbHeight(),
                    z + Math.sin(angle) * radius,
                    -Math.cos(angle) * 0.009D, 0.018D + random.nextDouble() * 0.026D,
                    -Math.sin(angle) * 0.009D);
        }
        for (int i = 0; i < 16; i++) {
            scatter(level, target, Elemental_reaction.DARK_SPARK_PARTICLE.get(), 0.085D);
        }
        for (int i = 0; i < 9; i++) {
            scatter(level, target, Elemental_reaction.SOUL_SHARD_PARTICLE.get(), 0.05D);
        }
    }

    public static void tick(ServerLevel level, LivingEntity target) {
        RandomSource random = level.getRandom();
        double angle = random.nextDouble() * Math.PI * 2.0D;
        double radius = Math.max(0.18D, target.getBbWidth() * 0.48D);
        double x = target.getX() + Math.cos(angle) * radius;
        double y = target.getY() + random.nextDouble() * target.getBbHeight() * 0.85D;
        double z = target.getZ() + Math.sin(angle) * radius;
        send(level, Elemental_reaction.SOUL_FLAME_PARTICLE.get(), x, y, z,
                -Math.cos(angle) * 0.011D, 0.018D, -Math.sin(angle) * 0.011D);

        if ((level.getGameTime() + target.getId()) % 3L == 0L) {
            send(level, Elemental_reaction.BLACK_SMOKE_PARTICLE.get(), x, y, z,
                    (random.nextDouble() - 0.5D) * 0.018D, 0.022D,
                    (random.nextDouble() - 0.5D) * 0.018D);
        }
        if ((level.getGameTime() + target.getId()) % 6L == 0L) {
            scatter(level, target, Elemental_reaction.SOUL_SHARD_PARTICLE.get(), 0.03D);
        }
    }

    public static void damageTick(ServerLevel level, LivingEntity target) {
        if ((level.getGameTime() + target.getId()) % 5L != 0L) {
            return;
        }
        for (int i = 0; i < 3; i++) {
            scatter(level, target, Elemental_reaction.SOUL_SPARK_PARTICLE.get(), 0.035D);
        }
        scatter(level, target, Elemental_reaction.DARK_SPARK_PARTICLE.get(), 0.035D);
    }

    public static void conversion(ServerLevel level, LivingEntity target) {
        double y = target.getY() + target.getBbHeight() * 0.5D;
        send(level, Elemental_reaction.SOUL_RING_PARTICLE.get(),
                target.getX(), y, target.getZ(), 0.0D, 0.0D, 0.0D);
        for (int i = 0; i < 22; i++) {
            scatter(level, target, Elemental_reaction.SOUL_FLAME_PARTICLE.get(), 0.11D);
            scatter(level, target, Elemental_reaction.BLACK_SMOKE_PARTICLE.get(), 0.055D);
        }
    }

    private static void scatter(ServerLevel level, LivingEntity target,
                                SimpleParticleType particle, double speed) {
        RandomSource random = level.getRandom();
        double angle = random.nextDouble() * Math.PI * 2.0D;
        double radius = random.nextDouble() * Math.max(0.22D, target.getBbWidth() * 0.58D);
        send(level, particle,
                target.getX() + Math.cos(angle) * radius,
                target.getY() + random.nextDouble() * target.getBbHeight(),
                target.getZ() + Math.sin(angle) * radius,
                Math.cos(angle) * speed, (random.nextDouble() - 0.3D) * speed,
                Math.sin(angle) * speed);
    }

    private static void send(ServerLevel level, SimpleParticleType particle,
                             double x, double y, double z,
                             double vx, double vy, double vz) {
        level.sendParticles(particle, x, y, z, 0, vx, vy, vz, 0.0D);
    }
}
