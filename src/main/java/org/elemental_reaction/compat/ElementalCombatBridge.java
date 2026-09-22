package org.elemental_reaction.compat;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Event;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

public final class ElementalCombatBridge {
    private static final String ATTACK_DATA_API_CLASS = "Tavi007.ElementalCombat.api.AttackDataAPI";
    private static final String ATTACK_LAYER_CLASS = "Tavi007.ElementalCombat.capabilities.attack.AttackLayer";
    private static final String ATTACK_DATA_CLASS = "Tavi007.ElementalCombat.capabilities.attack.AttackData";
    private static final String ELEMENTIFY_EVENT_CLASS = "Tavi007.ElementalCombat.api.ElementifyDamageSourceEvent";

    private static Method getLayerFromDamageSource;
    private static Method getLayerFromLivingEntity;
    private static Method getLayerFromProjectile;
    private static Method getLayerFromItemStack;
    private static Method getElement;
    private static Method attackDataToLayer;
    private static Method getAttackDataElement;
    private static boolean resolved;
    private static final Map<DamageSource, String> CAPTURED_DAMAGE_ELEMENTS = Collections.synchronizedMap(new WeakHashMap<>());
    private static final ThreadLocal<Map<DamageSource, String>> SYNTHETIC_DAMAGE_ELEMENTS = ThreadLocal.withInitial(HashMap::new);

    private ElementalCombatBridge() {
    }

    public static Optional<String> findDamageElement(DamageSource source) {
        String syntheticElement = SYNTHETIC_DAMAGE_ELEMENTS.get().get(source);
        if (isUsableElement(syntheticElement)) {
            return Optional.of(syntheticElement);
        }

        String capturedElement = CAPTURED_DAMAGE_ELEMENTS.get(source);
        if (isUsableElement(capturedElement)) {
            return Optional.of(capturedElement);
        }

        Optional<String> damageSourceElement = usable(findElementFromDamageSource(source));
        if (damageSourceElement.isPresent()) {
            return damageSourceElement;
        }

        Entity attacker = source.getEntity();
        Entity directEntity = source.getDirectEntity();
        Optional<String> attackerElement = usable(findElementFromEntity(attacker));
        if (attackerElement.isPresent()) {
            return attackerElement;
        }

        Optional<String> directElement = usable(findElementFromEntity(directEntity));
        if (directElement.isPresent()) {
            return directElement;
        }

        if (attacker instanceof LivingEntity livingEntity) {
            Optional<String> itemElement = usable(findElementFromItem(livingEntity));
            if (itemElement.isPresent()) {
                return itemElement;
            }
        }

        if (directEntity instanceof Projectile projectile && projectile.getOwner() instanceof LivingEntity owner) {
            return usable(findElementFromItem(owner));
        }
        return Optional.empty();
    }

    public static void withSyntheticDamageElement(DamageSource source, String element, Runnable action) {
        if (source == null || !isUsableElement(element)) {
            action.run();
            return;
        }

        Map<DamageSource, String> syntheticElements = SYNTHETIC_DAMAGE_ELEMENTS.get();
        boolean hadPrevious = syntheticElements.containsKey(source);
        String previous = syntheticElements.put(source, element);
        try {
            action.run();
        } finally {
            if (hadPrevious) {
                syntheticElements.put(source, previous);
            } else {
                syntheticElements.remove(source);
            }
        }
    }

    public static void captureElementifyEvent(Event event) {
        if (!ELEMENTIFY_EVENT_CLASS.equals(event.getClass().getName())) {
            return;
        }
        try {
            Method getDamageSource = event.getClass().getMethod("getDamageSource");
            Object damageSource = getDamageSource.invoke(event);
            if (!(damageSource instanceof DamageSource source)) {
                return;
            }

            Field attackDataField = event.getClass().getDeclaredField("attackData");
            attackDataField.setAccessible(true);
            Object attackData = attackDataField.get(event);
            Optional<String> element = readElementFromAttackData(
                    attackData,
                    source.getEntity(),
                    source.getDirectEntity(),
                    source.getEntity() == null ? null : source.getEntity().level()
            );
            element.ifPresent(value -> CAPTURED_DAMAGE_ELEMENTS.put(source, value));
        } catch (ReflectiveOperationException ignored) {
        }
    }

    public static Optional<String> findElementFromDamageSource(DamageSource source) {
        resolve();
        return readElement(getLayerFromDamageSource, source);
    }

    public static Optional<String> findElementFromLivingEntity(LivingEntity entity) {
        resolve();
        return readElement(getLayerFromLivingEntity, entity);
    }

    public static Optional<String> findElementFromProjectile(Projectile projectile) {
        resolve();
        return readElement(getLayerFromProjectile, projectile);
    }

    public static Optional<String> findElementFromItemStack(ItemStack stack, LivingEntity holder) {
        resolve();
        return readElement(getLayerFromItemStack, stack);
    }

    public static ResourceLocation getDamageTypeId(DamageSource source) {
        return source.typeHolder().unwrapKey().map(key -> key.location()).orElse(new ResourceLocation("empty"));
    }

    private static Optional<String> readElement(Method layerMethod, Object owner) {
        if (layerMethod == null || getElement == null || owner == null) {
            return Optional.empty();
        }
        try {
            Object attackLayer = layerMethod.invoke(null, owner);
            Object element = getElement.invoke(attackLayer);
            if (element instanceof String value && !value.isBlank()) {
                return Optional.of(value);
            }
        } catch (ReflectiveOperationException ignored) {
            return Optional.empty();
        }
        return Optional.empty();
    }

    private static Optional<String> findElementFromEntity(Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            return findElementFromLivingEntity(livingEntity);
        }
        if (entity instanceof Projectile projectile) {
            Optional<String> projectileElement = findElementFromProjectile(projectile);
            if (projectileElement.isPresent()) {
                return projectileElement;
            }
            if (projectile.getOwner() instanceof LivingEntity owner) {
                return findElementFromLivingEntity(owner);
            }
        }
        return Optional.empty();
    }

    private static Optional<String> findElementFromItem(LivingEntity entity) {
        Optional<String> mainHandElement = findElementFromItemStack(entity.getItemInHand(InteractionHand.MAIN_HAND), entity);
        if (mainHandElement.isPresent()) {
            return mainHandElement;
        }
        return findElementFromItemStack(entity.getItemInHand(InteractionHand.OFF_HAND), entity);
    }

    private static Optional<String> readElementFromAttackData(Object attackData, Entity attacker,
                                                               Entity directEntity, Level level) {
        resolve();
        if (attackData == null || getElement == null) {
            return Optional.empty();
        }

        try {
            if (attackDataToLayer != null) {
                Object layer = invokeAttackDataMethod(attackDataToLayer, attackData, attacker, level);
                Optional<String> layerElement = readElementFromLayer(layer);
                if (layerElement.isPresent()) {
                    return layerElement;
                }
            }

            if (getAttackDataElement != null) {
                Object element = invokeAttackDataMethod(getAttackDataElement, attackData, attacker, level);
                if (element instanceof String value && !value.isBlank()) {
                    return Optional.of(value);
                }
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return Optional.empty();
    }

    private static Object invokeAttackDataMethod(Method method, Object attackData, Entity attacker, Level level)
            throws ReflectiveOperationException {
        if (method.getParameterCount() == 0) {
            return method.invoke(attackData);
        }
        if (method.getParameterCount() == 3) {
            return method.invoke(attackData, attacker, null, level);
        }
        return null;
    }

    private static Optional<String> readElementFromLayer(Object layer) {
        if (layer == null || getElement == null) {
            return Optional.empty();
        }
        try {
            Object element = getElement.invoke(layer);
            if (element instanceof String value && !value.isBlank()) {
                return Optional.of(value);
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return Optional.empty();
    }

    private static Optional<String> usable(Optional<String> element) {
        return element.filter(ElementalCombatBridge::isUsableElement);
    }

    private static boolean isUsableElement(String element) {
        return element != null && !element.isBlank() && !"normal".equalsIgnoreCase(element);
    }

    private static void resolve() {
        if (resolved) {
            return;
        }
        resolved = true;
        try {
            Class<?> apiClass = Class.forName(ATTACK_DATA_API_CLASS);
            Class<?> layerClass = Class.forName(ATTACK_LAYER_CLASS);
            Class<?> attackDataClass = Class.forName(ATTACK_DATA_CLASS);
            getLayerFromDamageSource = apiClass.getMethod("getFullDataAsLayer", DamageSource.class);
            getLayerFromLivingEntity = apiClass.getMethod("getFullDataAsLayer", LivingEntity.class);
            getLayerFromProjectile = apiClass.getMethod("getFullDataAsLayer", Projectile.class);
            getLayerFromItemStack = apiClass.getMethod("getFullDataAsLayer", ItemStack.class);
            getElement = layerClass.getMethod("getElement");
            attackDataToLayer = findAttackDataMethod(attackDataClass, "toLayer");
            try {
                getAttackDataElement = findAttackDataMethod(attackDataClass, "getElement");
            } catch (NoSuchMethodException ignored) {
                getAttackDataElement = null;
            }
        } catch (ReflectiveOperationException ignored) {
            getLayerFromDamageSource = null;
            getLayerFromLivingEntity = null;
            getLayerFromProjectile = null;
            getLayerFromItemStack = null;
            getElement = null;
            attackDataToLayer = null;
            getAttackDataElement = null;
        }
    }

    private static Method findAttackDataMethod(Class<?> attackDataClass, String name) throws NoSuchMethodException {
        try {
            return attackDataClass.getMethod(name);
        } catch (NoSuchMethodException ignored) {
            return attackDataClass.getMethod(name, Entity.class, ItemStack.class, Level.class);
        }
    }
}
