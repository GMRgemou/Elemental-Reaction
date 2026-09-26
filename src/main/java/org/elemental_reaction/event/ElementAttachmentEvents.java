package org.elemental_reaction.event;

import org.elemental_reaction.Elemental_reaction;
import org.elemental_reaction.capability.ElementAttachment;
import org.elemental_reaction.capability.ElementAttachmentCapability;
import org.elemental_reaction.compat.ElementalCombatBridge;
import org.elemental_reaction.element.ElementClassifier;
import org.elemental_reaction.element.ElementColors;
import org.elemental_reaction.element.ElementKind;
import org.elemental_reaction.element.ElementTypes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = Elemental_reaction.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ElementAttachmentEvents {
    public static final ResourceLocation ATTACHMENT_CAPABILITY_ID = new ResourceLocation(Elemental_reaction.MODID, "element_attachment");
    public static final int ATTACHMENT_DURATION_TICKS = 20 * 5;

    private ElementAttachmentEvents() {
    }

    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof LivingEntity && !event.getCapabilities().containsKey(ATTACHMENT_CAPABILITY_ID)) {
            event.addCapability(ATTACHMENT_CAPABILITY_ID, new ElementAttachmentCapability());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void captureElementifiedDamage(Event event) {
        ElementalCombatBridge.captureElementifyEvent(event);
    }

    @SubscribeEvent
    public static void expireAttachment(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.hasEffect(Elemental_reaction.FROZEN.get())) {
            freezeMovement(entity);
        }

        if (entity.level().isClientSide) {
            return;
        }

        ElementExplosionHandler.tickFrozen(entity);
        ElementExplosionHandler.tickMudflow(entity);
        ElementExplosionHandler.tickSoulScorch(entity);
        ElementExplosionHandler.tickTurbulence(entity);

        long gameTime = entity.level().getGameTime();
        getAttachment(entity).ifPresent(attachment -> {
            if (!attachment.isPresent(gameTime) && !attachment.getElement().isBlank()) {
                attachment.clear();
                return;
            }
            if (attachment.isPresent(gameTime) && entity.level() instanceof ServerLevel serverLevel) {
                spawnAttachmentParticles(serverLevel, entity, attachment.getElement());
            }
        });
    }

    @SubscribeEvent
    public static void amplifyImbalancedKnockback(LivingKnockBackEvent event) {
        if (ElementExplosionHandler.isSoulScorchKnockbackSuppressed()) {
            event.setCanceled(true);
            return;
        }

        if (event.getEntity().hasEffect(Elemental_reaction.IMBALANCED.get())) {
            event.setStrength(event.getStrength() * 2.0F);
        }
        if (event.getEntity().hasEffect(Elemental_reaction.FROZEN.get())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void handleElementDamage(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide) {
            return;
        }

        DamageSource source = event.getSource();
        Optional<String> incomingElement = ElementalCombatBridge.findDamageElement(source)
                .filter(element -> !ElementTypes.isDefaultElement(element));
        if (incomingElement.isEmpty()) {
            return;
        }

        if (target.hasEffect(Elemental_reaction.ELEMENTAL_VULNERABLE.get())) {
            event.setAmount(event.getAmount() * 1.2F);
        }

        if (ElementExplosionHandler.isReactionDamageSuppressed()) {
            return;
        }

        String element = incomingElement.get();
        Optional<ElementKind> kind = ElementClassifier.kindOf(element);
        if (kind.isEmpty()) {
            return;
        }

        getAttachment(target).ifPresent(attachment -> processElementDamage(target, attachment, element, kind.get(), source, event.getAmount()));
    }

    private static void processElementDamage(LivingEntity target, ElementAttachment attachment, String incomingElement, ElementKind kind, DamageSource source, float damageAmount) {
        long gameTime = target.level().getGameTime();
        if (attachment.isElementalCalm(gameTime)) {
            return;
        }

        if (attachment.isPresent(gameTime)) {
            String attachedElement = attachment.getElement();
            if ("thunder".equals(incomingElement) || !attachedElement.equals(incomingElement)) {
                ElementExplosionHandler.trigger(target, attachedElement, incomingElement, source, damageAmount);
                return;
            }
        }

        if (kind == ElementKind.INERT) {
            attachment.attach(incomingElement, gameTime + ATTACHMENT_DURATION_TICKS);
        }
    }

    private static LazyOptional<ElementAttachment> getAttachment(LivingEntity entity) {
        return entity.getCapability(ElementAttachmentCapability.INSTANCE);
    }

    private static void freezeMovement(LivingEntity entity) {
        entity.setDeltaMovement(0.0D, 0.0D, 0.0D);
        entity.hasImpulse = true;
        entity.setJumping(false);
    }

    private static void spawnAttachmentParticles(ServerLevel level, LivingEntity entity, String element) {
        ElementColors.ElementColor color = ElementColors.forElement(element);
        double baseAngle = entity.tickCount * 0.2D;
        double height = entity.getBbHeight();
        double radius = Math.max(0.35D, entity.getBbWidth() * 0.55D);

        for (int index = 0; index < 3; index++) {
            double angle = baseAngle + index * Math.PI + entity.getRandom().nextDouble() * 0.35D;
            double distance = radius * (0.85D + entity.getRandom().nextDouble() * 0.45D);
            double x = entity.getX() + Math.cos(angle) * distance;
            double y = entity.getY() + 0.15D + entity.getRandom().nextDouble() * Math.max(0.2D, height);
            double z = entity.getZ() + Math.sin(angle) * distance;

            // With count zero, RGB arrives as xDist/yDist/zDist multiplied by speed.
            level.sendParticles(
                    Elemental_reaction.ELEMENT_ATTACHMENT_PARTICLE.get(),
                    x, y, z, 0,
                    color.red(), color.green(), color.blue(), 1.0D
            );
        }
    }
}
