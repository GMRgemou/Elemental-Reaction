package org.elemental_reaction.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

@FunctionalInterface
public interface SoulScorchParticleEffect {
    SoulScorchParticleEffect NONE = (level, target, attachedElement, incomingElement) -> {
    };

    void spawn(ServerLevel level, LivingEntity target, String attachedElement, String incomingElement);

    default void tick(ServerLevel level, LivingEntity target) {
    }

    default void damageTick(ServerLevel level, LivingEntity target) {
    }

    default void conversion(ServerLevel level, LivingEntity target) {
    }
}
