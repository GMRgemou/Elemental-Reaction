package org.elemental_reaction.event;

import org.elemental_reaction.capability.ElementAttachmentCapability;
import org.elemental_reaction.Elemental_reaction;
import org.elemental_reaction.compat.ElementalCombatBridge;
import org.elemental_reaction.element.ElementColors;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ElementExplosionHandler {
    private static final long ELEMENTAL_CALM_DURATION_TICKS = 20L * 3L;
    private static final int BURNING_SECONDS = 3;
    private static final double DIFFUSION_RANGE = 5.0D;
    private static final double DIFFUSION_KNOCKBACK_STRENGTH = 0.8D;
    private static final double DIFFUSION_MIN_HORIZONTAL_SPEED = 0.36D;
    private static final double SHOCKWAVE_RANGE = 3.0D;
    private static final float SHOCKWAVE_DAMAGE_MULTIPLIER = 0.5F;
    private static final int ELEMENTAL_VULNERABLE_DURATION_TICKS = 20 * 3;
    private static final int IMBALANCED_DURATION_TICKS = 20 * 5;
    private static final int REACTION_TEXT_DURATION_TICKS = 20;
    private static final int MUDFLOW_DURATION_TICKS = 20 * 3;
    private static final int MUDFLOW_INTERVAL_TICKS = 20;
    private static final int MUDFLOW_TICKS = 3;
    private static final float MUDFLOW_DAMAGE_MULTIPLIER = 0.1F;
    private static final float TURBULENCE_DAMAGE_MULTIPLIER = 0.2F;
    private static final double REACTION_TEXT_RISE_PER_TICK = 0.035D;
    private static final ThreadLocal<Boolean> SUPPRESS_REACTION_DAMAGE = ThreadLocal.withInitial(() -> false);
    private static final Map<UUID, MudflowInstance> ACTIVE_MUDFLOWS = new HashMap<>();
    private static final Map<UUID, ReactionTextInstance> ACTIVE_REACTION_TEXTS = new HashMap<>();
    private static volatile DiffusionParticleEffect diffusionParticleEffect = DiffusionParticleEffect.NONE;
    private static volatile BurningReactionEffect burningReactionEffect = BurningReactionEffect.NONE;
    private static volatile MudflowParticleEffect mudflowParticleEffect = MudflowParticleEffect.NONE;
    private static volatile TurbulenceParticleEffect turbulenceParticleEffect = TurbulenceParticleEffect.NONE;

    private ElementExplosionHandler() {
    }

    public static void setDiffusionParticleEffect(DiffusionParticleEffect effect) {
        diffusionParticleEffect = effect == null ? DiffusionParticleEffect.NONE : effect;
    }

    public static void setBurningReactionEffect(BurningReactionEffect effect) {
        burningReactionEffect = effect == null ? BurningReactionEffect.NONE : effect;
    }

    public static void setMudflowParticleEffect(MudflowParticleEffect effect) {
        mudflowParticleEffect = effect == null ? MudflowParticleEffect.NONE : effect;
    }

    public static void setTurbulenceParticleEffect(TurbulenceParticleEffect effect) {
        turbulenceParticleEffect = effect == null ? TurbulenceParticleEffect.NONE : effect;
    }

    public static boolean isReactionDamageSuppressed() {
        return SUPPRESS_REACTION_DAMAGE.get();
    }

    public static void tickMudflow(LivingEntity target) {
        if (target.level().isClientSide) {
            return;
        }

        MudflowInstance mudflow = ACTIVE_MUDFLOWS.get(target.getUUID());
        if (mudflow == null) {
            return;
        }

        if (!target.isAlive() || mudflow.remainingTicks <= 0) {
            ACTIVE_MUDFLOWS.remove(target.getUUID());
            return;
        }

        long gameTime = target.level().getGameTime();
        if (gameTime < mudflow.nextDamageAt) {
            return;
        }

        dealMudflowTickDamage(target, mudflow);
        mudflow.remainingTicks--;
        mudflow.nextDamageAt += MUDFLOW_INTERVAL_TICKS;

        if (mudflow.remainingTicks <= 0) {
            ACTIVE_MUDFLOWS.remove(target.getUUID());
        }
    }

    public static void tickReactionTextDisplays(ServerLevel level) {
        Iterator<Map.Entry<UUID, ReactionTextInstance>> iterator = ACTIVE_REACTION_TEXTS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, ReactionTextInstance> entry = iterator.next();
            ReactionTextInstance text = entry.getValue();
            if (!text.dimension.equals(level.dimension())) {
                continue;
            }

            Entity entity = level.getEntity(entry.getKey());
            if (entity == null || entity.isRemoved()) {
                iterator.remove();
                continue;
            }

            text.age++;
            if (text.age >= REACTION_TEXT_DURATION_TICKS) {
                entity.discard();
                iterator.remove();
                continue;
            }

            entity.setPos(entity.getX(), entity.getY() + REACTION_TEXT_RISE_PER_TICK, entity.getZ());
        }
    }

    public static void trigger(LivingEntity target, String attachedElement, String incomingElement, DamageSource source, float damageAmount) {
        target.getCapability(ElementAttachmentCapability.INSTANCE).ifPresent(attachment -> {
            long elementalCalmUntil = target.level().getGameTime() + ELEMENTAL_CALM_DURATION_TICKS;
            attachment.startElementalCalm(elementalCalmUntil);
            attachment.clear();
        });

        if (isBurningReaction(attachedElement, incomingElement)) {
            burn(target, attachedElement, incomingElement, damageAmount);
        }

        if ("wind".equals(incomingElement)) {
            diffuse(target, attachedElement, source, damageAmount);
        }

        if ("thunder".equals(incomingElement)) {
            shock(target, source, damageAmount);
        }

        if (isMudflowReaction(attachedElement, incomingElement)) {
            mudflow(target, attachedElement, incomingElement, source, damageAmount);
        }

        if (isTurbulenceReaction(attachedElement, incomingElement)) {
            turbulence(target, attachedElement, incomingElement, source, damageAmount);
        }
    }

    private static boolean isBurningReaction(String attachedElement, String incomingElement) {
        return "fire".equals(attachedElement) && "flora".equals(incomingElement)
                || "flora".equals(attachedElement) && "fire".equals(incomingElement);
    }

    private static boolean isMudflowReaction(String attachedElement, String incomingElement) {
        return "water".equals(attachedElement) && "earth".equals(incomingElement)
                || "earth".equals(attachedElement) && "water".equals(incomingElement);
    }

    private static boolean isTurbulenceReaction(String attachedElement, String incomingElement) {
        return "water".equals(attachedElement) && "darkness".equals(incomingElement)
                || "darkness".equals(attachedElement) && "water".equals(incomingElement);
    }

    private static void burn(LivingEntity target, String attachedElement, String incomingElement, float damageAmount) {
        if (target.level().isClientSide) {
            return;
        }

        target.setSecondsOnFire(BURNING_SECONDS);
        target.hurt(target.damageSources().onFire(), damageAmount);
        spawnReactionName(target, "燃烧");

        if (target.level() instanceof ServerLevel serverLevel) {
            burningReactionEffect.spawn(serverLevel, target, attachedElement, incomingElement);
        }
    }

    private static void diffuse(LivingEntity source, String element, DamageSource damageSource, float damageAmount) {
        if (source.level().isClientSide) {
            return;
        }

        Vec3 direction = getDiffusionDirection(source, damageSource);
        Vec3 sourceCenter = entityCenter(source);
        AABB searchBox = source.getBoundingBox().inflate(DIFFUSION_RANGE);
        long gameTime = source.level().getGameTime();
        List<LivingEntity> affectedTargets = new ArrayList<>();

        for (LivingEntity candidate : source.level().getEntitiesOfClass(
                LivingEntity.class,
                searchBox,
                entity -> entity != source && entity.isAlive())) {
            Vec3 offset = horizontal(entityCenter(candidate).subtract(sourceCenter));
            if (offset.lengthSqr() > DIFFUSION_RANGE * DIFFUSION_RANGE || offset.dot(direction) <= 0.0D) {
                continue;
            }

            applyDiffusedElement(candidate, element, gameTime, damageSource, damageAmount);
            knockBack(candidate, direction);
            affectedTargets.add(candidate);
        }

        if (source.level() instanceof ServerLevel serverLevel) {
            spawnReactionName(source, "扩散");
            diffusionParticleEffect.spawn(serverLevel, source, affectedTargets, element, direction);
        }
    }

    private static void shock(LivingEntity source, DamageSource damageSource, float damageAmount) {
        if (source.level().isClientSide || damageAmount <= 0.0F) {
            return;
        }

        Vec3 sourceCenter = entityCenter(source);
        AABB searchBox = source.getBoundingBox().inflate(SHOCKWAVE_RANGE);
        List<LivingEntity> affectedTargets = new ArrayList<>();
        affectedTargets.add(source);

        for (LivingEntity candidate : source.level().getEntitiesOfClass(
                LivingEntity.class,
                searchBox,
                entity -> entity != source && entity.isAlive())) {
            Vec3 offset = entityCenter(candidate).subtract(sourceCenter);
            if (offset.lengthSqr() <= SHOCKWAVE_RANGE * SHOCKWAVE_RANGE) {
                affectedTargets.add(candidate);
            }
        }

        boolean previousSuppression = SUPPRESS_REACTION_DAMAGE.get();
        SUPPRESS_REACTION_DAMAGE.set(true);
        try {
            float shockDamage = damageAmount * SHOCKWAVE_DAMAGE_MULTIPLIER;
            for (LivingEntity target : affectedTargets) {
                hurtIgnoringInvulnerability(target, damageSource, shockDamage);
                target.addEffect(new MobEffectInstance(
                        Elemental_reaction.ELEMENTAL_VULNERABLE.get(),
                        ELEMENTAL_VULNERABLE_DURATION_TICKS,
                        0,
                        false,
                        true,
                        true
                ));
            }
        } finally {
            SUPPRESS_REACTION_DAMAGE.set(previousSuppression);
        }

        spawnReactionName(source, "雷震");

        if (source.level() instanceof ServerLevel serverLevel) {
            spawnShockwaveParticles(serverLevel, source);
        }
    }

    private static void mudflow(LivingEntity target, String attachedElement, String incomingElement,
                                DamageSource damageSource, float damageAmount) {
        if (target.level().isClientSide || damageAmount <= 0.0F) {
            return;
        }

        long firstDamageAt = target.level().getGameTime() + MUDFLOW_INTERVAL_TICKS;
        ACTIVE_MUDFLOWS.put(target.getUUID(), new MudflowInstance(
                damageSource,
                damageAmount * MUDFLOW_DAMAGE_MULTIPLIER,
                firstDamageAt,
                MUDFLOW_TICKS
        ));
        target.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN,
                MUDFLOW_DURATION_TICKS,
                1,
                false,
                true,
                true
        ));
        spawnReactionName(target, "泥流");

        if (target.level() instanceof ServerLevel serverLevel) {
            mudflowParticleEffect.spawn(serverLevel, target, attachedElement, incomingElement);
        }
    }

    private static void turbulence(LivingEntity target, String attachedElement, String incomingElement,
                                   DamageSource damageSource, float damageAmount) {
        if (target.level().isClientSide || damageAmount <= 0.0F) {
            return;
        }

        target.addEffect(new MobEffectInstance(
                Elemental_reaction.IMBALANCED.get(),
                IMBALANCED_DURATION_TICKS,
                0,
                false,
                true,
                true
        ));

        boolean previousSuppression = SUPPRESS_REACTION_DAMAGE.get();
        SUPPRESS_REACTION_DAMAGE.set(true);
        try {
            ElementalCombatBridge.withSyntheticDamageElement(damageSource, "water",
                    () -> hurtIgnoringInvulnerability(target, damageSource, damageAmount * TURBULENCE_DAMAGE_MULTIPLIER));
        } finally {
            SUPPRESS_REACTION_DAMAGE.set(previousSuppression);
        }

        spawnReactionName(target, "乱流");

        if (target.level() instanceof ServerLevel serverLevel) {
            turbulenceParticleEffect.spawn(serverLevel, target, attachedElement, incomingElement);
        }
    }

    private static void dealMudflowTickDamage(LivingEntity target, MudflowInstance mudflow) {
        boolean previousSuppression = SUPPRESS_REACTION_DAMAGE.get();
        SUPPRESS_REACTION_DAMAGE.set(true);
        try {
            ElementalCombatBridge.withSyntheticDamageElement(mudflow.damageSource, "earth",
                    () -> hurtIgnoringInvulnerability(target, mudflow.damageSource, mudflow.damagePerElement));
            ElementalCombatBridge.withSyntheticDamageElement(mudflow.damageSource, "water",
                    () -> hurtIgnoringInvulnerability(target, mudflow.damageSource, mudflow.damagePerElement));
        } finally {
            SUPPRESS_REACTION_DAMAGE.set(previousSuppression);
        }
    }

    private static void hurtIgnoringInvulnerability(LivingEntity target, DamageSource damageSource, float damageAmount) {
        int previousInvulnerableTime = target.invulnerableTime;
        target.invulnerableTime = 0;
        target.hurt(damageSource, damageAmount);
        target.invulnerableTime = Math.max(target.invulnerableTime, previousInvulnerableTime);
    }

    private static void spawnShockwaveParticles(ServerLevel level, LivingEntity source) {
        ElementColors.ElementColor color = ElementColors.forElement("thunder");
        Vec3 center = entityCenter(source);
        int particleCount = 48;
        for (int index = 0; index < particleCount; index++) {
            double angle = Math.PI * 2.0D * index / particleCount;
            Vec3 position = center.add(
                    Math.cos(angle) * SHOCKWAVE_RANGE,
                    0.0D,
                    Math.sin(angle) * SHOCKWAVE_RANGE
            );
            level.sendParticles(
                    Elemental_reaction.ELEMENT_DIFFUSION_PARTICLE.get(),
                    position.x, position.y, position.z, 0,
                    color.red(), color.green(), color.blue(), 1.0D
            );
        }
    }

    private static void applyDiffusedElement(LivingEntity target, String element, long gameTime,
                                              DamageSource damageSource, float damageAmount) {
        target.getCapability(ElementAttachmentCapability.INSTANCE).ifPresent(attachment -> {
            if (attachment.isElementalCalm(gameTime)) {
                return;
            }

            if (attachment.isPresent(gameTime)) {
                String attachedElement = attachment.getElement();
                if (!attachedElement.equals(element)) {
                    trigger(target, attachedElement, element, damageSource, damageAmount);
                } else {
                    attachment.attach(element, gameTime + ElementAttachmentEvents.ATTACHMENT_DURATION_TICKS);
                }
                return;
            }

            if (!element.isBlank()) {
                attachment.attach(element, gameTime + ElementAttachmentEvents.ATTACHMENT_DURATION_TICKS);
            }
        });
    }

    private static void spawnReactionName(LivingEntity target, String reactionName) {
        if (!(target.level() instanceof ServerLevel level)) {
            return;
        }

        Vec3 position = reactionTextPosition(target);
        ArmorStand text = new ArmorStand(level, position.x, position.y, position.z);
        Component name = Component.literal(reactionName);
        text.setCustomName(name);
        text.setCustomNameVisible(true);
        text.setInvisible(true);
        text.setNoGravity(true);
        text.setSilent(true);

        CompoundTag tag = text.saveWithoutId(new CompoundTag());
        tag.putBoolean("Invisible", true);
        tag.putBoolean("Marker", true);
        tag.putBoolean("NoGravity", true);
        tag.putBoolean("CustomNameVisible", true);
        tag.putString("CustomName", Component.Serializer.toJson(name));
        text.load(tag);
        text.setPos(position.x, position.y, position.z);
        text.setCustomName(name);
        text.setCustomNameVisible(true);
        text.setInvisible(true);
        text.setNoGravity(true);
        text.setSilent(true);

        level.addFreshEntity(text);
        ACTIVE_REACTION_TEXTS.put(text.getUUID(), new ReactionTextInstance(level.dimension()));
    }

    private static Vec3 reactionTextPosition(LivingEntity target) {
        Vec3 look = horizontal(target.getLookAngle());
        if (look.lengthSqr() < 1.0E-6D) {
            look = horizontal(Vec3.directionFromRotation(0.0F, target.getYRot()));
        }

        Vec3 right = look.lengthSqr() < 1.0E-6D
                ? new Vec3(1.0D, 0.0D, 0.0D)
                : new Vec3(-look.z, 0.0D, look.x).normalize();
        double rightOffset = Math.max(0.45D, target.getBbWidth() * 0.65D);
        return target.position()
                .add(0.0D, target.getBbHeight() + 0.35D, 0.0D)
                .add(right.scale(rightOffset));
    }

    private static void knockBack(LivingEntity target, Vec3 direction) {
        Vec3 horizontalDirection = horizontal(direction);
        if (horizontalDirection.lengthSqr() < 1.0E-6D) {
            return;
        }

        horizontalDirection = horizontalDirection.normalize();
        target.knockback(DIFFUSION_KNOCKBACK_STRENGTH, -horizontalDirection.x, -horizontalDirection.z);

        Vec3 movement = target.getDeltaMovement();
        double speedAlongDirection = movement.x * horizontalDirection.x + movement.z * horizontalDirection.z;
        if (speedAlongDirection < DIFFUSION_MIN_HORIZONTAL_SPEED) {
            target.setDeltaMovement(movement.add(horizontalDirection.scale(DIFFUSION_MIN_HORIZONTAL_SPEED - speedAlongDirection)));
        }
        target.hasImpulse = true;
    }

    private static Vec3 getDiffusionDirection(LivingEntity target, DamageSource damageSource) {
        Vec3 targetCenter = entityCenter(target);
        Vec3 direction = awayFrom(targetCenter, damageSource.getEntity());
        if (direction != null) {
            return direction;
        }

        direction = awayFrom(targetCenter, damageSource.getDirectEntity());
        if (direction != null) {
            return direction;
        }

        Vec3 sourcePosition = damageSource.getSourcePosition();
        if (sourcePosition != null) {
            direction = horizontal(targetCenter.subtract(sourcePosition));
            if (direction.lengthSqr() >= 1.0E-6D) {
                return direction.normalize();
            }
        }

        return getBehindDirection(target);
    }

    private static Vec3 awayFrom(Vec3 targetCenter, Entity sourceEntity) {
        if (sourceEntity == null) {
            return null;
        }

        Vec3 direction = horizontal(targetCenter.subtract(entityCenter(sourceEntity)));
        if (direction.lengthSqr() < 1.0E-6D) {
            return null;
        }
        return direction.normalize();
    }

    private static Vec3 getBehindDirection(Entity entity) {
        if (entity == null) {
            return null;
        }

        Vec3 look = entity.getLookAngle();
        Vec3 horizontalLook = horizontal(look);
        if (horizontalLook.lengthSqr() < 1.0E-6D) {
            Vec3 movement = horizontal(entity.getDeltaMovement());
            if (movement.lengthSqr() < 1.0E-6D) {
                return null;
            }
            return movement.normalize().scale(-1.0D);
        }
        return horizontalLook.normalize().scale(-1.0D);
    }

    private static Vec3 horizontal(Vec3 vector) {
        return new Vec3(vector.x, 0.0D, vector.z);
    }

    private static Vec3 entityCenter(LivingEntity entity) {
        return entity.position().add(0.0D, entity.getBbHeight() * 0.5D, 0.0D);
    }

    private static Vec3 entityCenter(Entity entity) {
        return entity.position().add(0.0D, entity.getBbHeight() * 0.5D, 0.0D);
    }

    private static final class MudflowInstance {
        private final DamageSource damageSource;
        private final float damagePerElement;
        private long nextDamageAt;
        private int remainingTicks;

        private MudflowInstance(DamageSource damageSource, float damagePerElement, long nextDamageAt, int remainingTicks) {
            this.damageSource = damageSource;
            this.damagePerElement = damagePerElement;
            this.nextDamageAt = nextDamageAt;
            this.remainingTicks = remainingTicks;
        }
    }

    private static final class ReactionTextInstance {
        private final ResourceKey<Level> dimension;
        private int age;

        private ReactionTextInstance(ResourceKey<Level> dimension) {
            this.dimension = dimension;
        }
    }
}
