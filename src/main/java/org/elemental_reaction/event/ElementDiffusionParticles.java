package org.elemental_reaction.event;

import org.elemental_reaction.Elemental_reaction;
import org.elemental_reaction.element.ElementColors;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class ElementDiffusionParticles {
    private static final double DIFFUSION_RANGE = 5.0D;
    private static final int BASE_PARTICLES = 26;
    private static final int PARTICLES_PER_TARGET = 4;
    private static final int ATTACHMENT_BURST_PARTICLES = 12;
    private static final int MAX_TARGET_PARTICLE_BONUS = 16;

    private ElementDiffusionParticles() {
    }

    public static void spawn(ServerLevel level, LivingEntity source, List<LivingEntity> targets, String element, Vec3 direction) {
        ElementColors.ElementColor color = ElementColors.forElement(element);
        Vec3 origin = entityCenter(source);
        Vec3 forward = normalizeHorizontal(direction);
        Vec3 right = new Vec3(-forward.z, 0.0D, forward.x);
        RandomSource random = source.getRandom();

        int targetBonus = Math.min(targets.size() * PARTICLES_PER_TARGET, MAX_TARGET_PARTICLE_BONUS);
        for (int index = 0; index < BASE_PARTICLES + targetBonus; index++) {
            double distance = 0.25D + random.nextDouble() * DIFFUSION_RANGE;
            double angle = (random.nextDouble() - 0.5D) * Math.PI;
            double lift = (random.nextDouble() - 0.5D) * source.getBbHeight() * 0.55D;
            Vec3 position = origin
                    .add(forward.scale(Math.cos(angle) * distance))
                    .add(right.scale(Math.sin(angle) * distance))
                    .add(0.0D, lift, 0.0D);
            spawnParticle(level, position, color);
        }

        for (LivingEntity target : targets) {
            spawnTargetTrail(level, origin, target, color, random);
            spawnAttachmentBurst(level, target, color, random);
        }
    }

    private static void spawnTargetTrail(ServerLevel level, Vec3 origin, LivingEntity target,
                                         ElementColors.ElementColor color, RandomSource random) {
        Vec3 targetCenter = entityCenter(target);
        for (int index = 0; index < PARTICLES_PER_TARGET; index++) {
            double progress = 0.35D + random.nextDouble() * 0.65D;
            Vec3 position = origin.lerp(targetCenter, progress).add(
                    (random.nextDouble() - 0.5D) * 0.35D,
                    (random.nextDouble() - 0.5D) * 0.35D,
                    (random.nextDouble() - 0.5D) * 0.35D
            );
            spawnParticle(level, position, color);
        }
    }

    private static void spawnAttachmentBurst(ServerLevel level, LivingEntity target,
                                             ElementColors.ElementColor color, RandomSource random) {
        double height = target.getBbHeight();
        double radius = Math.max(0.35D, target.getBbWidth() * 0.55D);

        for (int index = 0; index < ATTACHMENT_BURST_PARTICLES; index++) {
            double angle = Math.PI * 2.0D * index / ATTACHMENT_BURST_PARTICLES + random.nextDouble() * 0.25D;
            double distance = radius * (0.65D + random.nextDouble() * 0.55D);
            double x = target.getX() + Math.cos(angle) * distance;
            double y = target.getY() + 0.1D + random.nextDouble() * Math.max(0.2D, height);
            double z = target.getZ() + Math.sin(angle) * distance;

            level.sendParticles(
                    Elemental_reaction.ELEMENT_ATTACHMENT_PARTICLE.get(),
                    x, y, z, 0,
                    color.red(), color.green(), color.blue(), 1.0D
            );
        }
    }

    private static void spawnParticle(ServerLevel level, Vec3 position, ElementColors.ElementColor color) {
        level.sendParticles(
                Elemental_reaction.ELEMENT_DIFFUSION_PARTICLE.get(),
                position.x, position.y, position.z, 0,
                color.red(), color.green(), color.blue(), 1.0D
        );
    }

    private static Vec3 normalizeHorizontal(Vec3 vector) {
        Vec3 horizontal = new Vec3(vector.x, 0.0D, vector.z);
        if (horizontal.lengthSqr() < 1.0E-6D) {
            return new Vec3(0.0D, 0.0D, -1.0D);
        }
        return horizontal.normalize();
    }

    private static Vec3 entityCenter(LivingEntity entity) {
        return entity.position().add(0.0D, entity.getBbHeight() * 0.5D, 0.0D);
    }
}
