package org.elemental_reaction.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;

@FunctionalInterface
public interface DiffusionParticleEffect {
    DiffusionParticleEffect NONE = (level, source, targets, element, direction) -> {
    };

    void spawn(ServerLevel level, LivingEntity source, List<LivingEntity> targets, String element, Vec3 direction);
}
