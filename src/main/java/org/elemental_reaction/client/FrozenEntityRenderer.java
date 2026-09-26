package org.elemental_reaction.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.client.event.EntityRenderersEvent;
import org.elemental_reaction.Elemental_reaction;

public final class FrozenEntityRenderer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    // A solid texture makes the tint cover the whole model. The ice block texture
    // contains transparent detail that becomes sparse when mapped onto entity UVs.
    private static final ResourceLocation FROZEN_OVERLAY_TEXTURE =
            new ResourceLocation(Elemental_reaction.MODID, "textures/particle/freeze_ice_shell.png");

    public FrozenEntityRenderer(RenderLayerParent<T, M> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                       T entity, float limbSwing, float limbSwingAmount, float partialTick,
                       float ageInTicks, float netHeadYaw, float headPitch) {
        if (!entity.hasEffect(Elemental_reaction.FROZEN.get()) || entity.isInvisible()) {
            return;
        }

        poseStack.pushPose();
        poseStack.scale(1.035F, 1.02F, 1.035F);
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityTranslucent(FROZEN_OVERLAY_TEXTURE));
        getParentModel().renderToBuffer(
                poseStack,
                buffer,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                0.55F,
                0.86F,
                1.0F,
                0.42F
        );
        poseStack.popPose();
    }

    public static void registerLayers(EntityRenderersEvent.AddLayers event) {
        for (String skin : event.getSkins()) {
            addLayer(event.getSkin(skin));
        }

        addLayer(event, EntityType.ALLAY);
        addLayer(event, EntityType.ARMOR_STAND);
        addLayer(event, EntityType.AXOLOTL);
        addLayer(event, EntityType.BAT);
        addLayer(event, EntityType.BEE);
        addLayer(event, EntityType.BLAZE);
        addLayer(event, EntityType.CAMEL);
        addLayer(event, EntityType.CAT);
        addLayer(event, EntityType.CAVE_SPIDER);
        addLayer(event, EntityType.CHICKEN);
        addLayer(event, EntityType.COD);
        addLayer(event, EntityType.COW);
        addLayer(event, EntityType.CREEPER);
        addLayer(event, EntityType.DOLPHIN);
        addLayer(event, EntityType.DONKEY);
        addLayer(event, EntityType.DROWNED);
        addLayer(event, EntityType.ELDER_GUARDIAN);
        addLayer(event, EntityType.ENDER_DRAGON);
        addLayer(event, EntityType.ENDERMAN);
        addLayer(event, EntityType.ENDERMITE);
        addLayer(event, EntityType.EVOKER);
        addLayer(event, EntityType.FOX);
        addLayer(event, EntityType.FROG);
        addLayer(event, EntityType.GHAST);
        addLayer(event, EntityType.GIANT);
        addLayer(event, EntityType.GLOW_SQUID);
        addLayer(event, EntityType.GOAT);
        addLayer(event, EntityType.GUARDIAN);
        addLayer(event, EntityType.HOGLIN);
        addLayer(event, EntityType.HORSE);
        addLayer(event, EntityType.HUSK);
        addLayer(event, EntityType.ILLUSIONER);
        addLayer(event, EntityType.IRON_GOLEM);
        addLayer(event, EntityType.LLAMA);
        addLayer(event, EntityType.MAGMA_CUBE);
        addLayer(event, EntityType.MOOSHROOM);
        addLayer(event, EntityType.MULE);
        addLayer(event, EntityType.OCELOT);
        addLayer(event, EntityType.PANDA);
        addLayer(event, EntityType.PARROT);
        addLayer(event, EntityType.PHANTOM);
        addLayer(event, EntityType.PIG);
        addLayer(event, EntityType.PIGLIN);
        addLayer(event, EntityType.PIGLIN_BRUTE);
        addLayer(event, EntityType.PILLAGER);
        addLayer(event, EntityType.POLAR_BEAR);
        addLayer(event, EntityType.PUFFERFISH);
        addLayer(event, EntityType.RABBIT);
        addLayer(event, EntityType.RAVAGER);
        addLayer(event, EntityType.SALMON);
        addLayer(event, EntityType.SHEEP);
        addLayer(event, EntityType.SHULKER);
        addLayer(event, EntityType.SILVERFISH);
        addLayer(event, EntityType.SKELETON);
        addLayer(event, EntityType.SKELETON_HORSE);
        addLayer(event, EntityType.SLIME);
        addLayer(event, EntityType.SNIFFER);
        addLayer(event, EntityType.SNOW_GOLEM);
        addLayer(event, EntityType.SPIDER);
        addLayer(event, EntityType.SQUID);
        addLayer(event, EntityType.STRAY);
        addLayer(event, EntityType.STRIDER);
        addLayer(event, EntityType.TADPOLE);
        addLayer(event, EntityType.TRADER_LLAMA);
        addLayer(event, EntityType.TROPICAL_FISH);
        addLayer(event, EntityType.TURTLE);
        addLayer(event, EntityType.VEX);
        addLayer(event, EntityType.VILLAGER);
        addLayer(event, EntityType.VINDICATOR);
        addLayer(event, EntityType.WANDERING_TRADER);
        addLayer(event, EntityType.WARDEN);
        addLayer(event, EntityType.WITCH);
        addLayer(event, EntityType.WITHER);
        addLayer(event, EntityType.WITHER_SKELETON);
        addLayer(event, EntityType.WOLF);
        addLayer(event, EntityType.ZOGLIN);
        addLayer(event, EntityType.ZOMBIE);
        addLayer(event, EntityType.ZOMBIE_HORSE);
        addLayer(event, EntityType.ZOMBIE_VILLAGER);
        addLayer(event, EntityType.ZOMBIFIED_PIGLIN);
    }

    private static void addLayer(EntityRenderersEvent.AddLayers event, EntityType<? extends LivingEntity> type) {
        EntityRenderer<?> renderer = event.getRenderer(type);
        addLayer(renderer);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void addLayer(Object renderer) {
        if (renderer instanceof LivingEntityRenderer livingRenderer) {
            livingRenderer.addLayer(new FrozenEntityRenderer(livingRenderer));
        }
    }
}
