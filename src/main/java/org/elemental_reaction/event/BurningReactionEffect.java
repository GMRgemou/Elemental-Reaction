package org.elemental_reaction.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

@FunctionalInterface
public interface BurningReactionEffect {
    BurningReactionEffect NONE = (level, target, attachedElement, incomingElement) -> {
    };

    void spawn(ServerLevel level, LivingEntity target, String attachedElement, String incomingElement);
}
