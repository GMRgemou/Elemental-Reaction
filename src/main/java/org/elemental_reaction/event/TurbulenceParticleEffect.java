package org.elemental_reaction.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

@FunctionalInterface
public interface TurbulenceParticleEffect {
    TurbulenceParticleEffect NONE = (level, target, attachedElement, incomingElement) -> {
    };

    void spawn(ServerLevel level, LivingEntity target, String attachedElement, String incomingElement);

    default void tick(ServerLevel level, LivingEntity target) {
    }
}
