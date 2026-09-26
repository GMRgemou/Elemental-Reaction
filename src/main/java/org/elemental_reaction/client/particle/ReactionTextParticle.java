package org.elemental_reaction.client.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.elemental_reaction.particle.ReactionTextParticleOptions;
import org.joml.Matrix4f;

import java.util.Locale;
import java.util.Map;

public final class ReactionTextParticle extends Particle {
    private static final int LIFETIME_TICKS = 20;
    private static final double RISE_PER_TICK = 0.035D;
    private static final float TEXT_SCALE = 0.025F;
    private static final Map<String, LocalizedReactionName> REACTION_NAMES = Map.of(
            "reaction.elemental_reaction.burning", new LocalizedReactionName("燃烧", "Burning"),
            "reaction.elemental_reaction.diffusion", new LocalizedReactionName("扩散", "Diffusion"),
            "reaction.elemental_reaction.shock", new LocalizedReactionName("雷震", "Shock"),
            "reaction.elemental_reaction.mudflow", new LocalizedReactionName("泥流", "Mudflow"),
            "reaction.elemental_reaction.turbulence", new LocalizedReactionName("乱流", "Turbulence"),
            "reaction.elemental_reaction.frozen", new LocalizedReactionName("冰冻", "Frozen"),
            "reaction.elemental_reaction.soul_scorch", new LocalizedReactionName("灼魂", "Soul Scorch")
    );
    private static final ReactionArtStyle FALLBACK_STYLE = new ReactionArtStyle(0xF7F7F7, 0x8FE8FF, 0x27405A, 0x89DFFF);
    private static final Map<String, ReactionArtStyle> REACTION_STYLES = Map.of(
            "reaction.elemental_reaction.burning", new ReactionArtStyle(0xFFE16A, 0xFF4A21, 0x5A1508, 0xFF8A2A),
            "reaction.elemental_reaction.diffusion", new ReactionArtStyle(0xB9FF8A, 0x47F0FF, 0x123E35, 0x7DFFC4),
            "reaction.elemental_reaction.shock", new ReactionArtStyle(0xFFF36B, 0xB98CFF, 0x3B245C, 0xFFE56A),
            "reaction.elemental_reaction.mudflow", new ReactionArtStyle(0xB9F07A, 0x8C633A, 0x2F2418, 0x9CDC65),
            "reaction.elemental_reaction.turbulence", new ReactionArtStyle(0x8FF7FF, 0x2D7DFF, 0x12385F, 0x70DEFF),
            "reaction.elemental_reaction.frozen", new ReactionArtStyle(0xF3FFFF, 0x6FB7FF, 0x173A62, 0xB7F5FF),
            "reaction.elemental_reaction.soul_scorch", new ReactionArtStyle(0xFF725E, 0x9B5CFF, 0x2B1237, 0xFF5F8A)
    );

    private final String text;

    private ReactionTextParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            String text
    ) {
        super(level, x, y, z);
        this.text = text;
        lifetime = LIFETIME_TICKS;
        yd = RISE_PER_TICK;
        friction = 1.0F;
        hasPhysics = false;
        setSize(0.01F, 0.01F);
    }

    @Override
    public void render(VertexConsumer ignored, Camera camera, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        Vec3 cameraPosition = camera.getPosition();

        float x = (float) (Mth.lerp(partialTick, xo, this.x) - cameraPosition.x);
        float y = (float) (Mth.lerp(partialTick, yo, this.y) - cameraPosition.y);
        float z = (float) (Mth.lerp(partialTick, zo, this.z) - cameraPosition.z);

        PoseStack poseStack = new PoseStack();
        poseStack.translate(x, y, z);
        poseStack.mulPose(camera.rotation());

        float lifeProgress = ((float) age + partialTick) / Math.max(lifetime, 1);
        float pulse = 1.0F + Mth.sin(lifeProgress * (float) Math.PI) * 0.12F;
        poseStack.scale(-TEXT_SCALE * pulse, -TEXT_SCALE * pulse, TEXT_SCALE * pulse);

        Matrix4f matrix = poseStack.last().pose();
        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        String displayText = localizeReactionText(minecraft, text);
        ReactionArtStyle style = REACTION_STYLES.getOrDefault(text, FALLBACK_STYLE);
        float textX = -font.width(displayText) / 2.0F;
        int alpha = textAlpha(lifeProgress);
        if (alpha <= 0) {
            return;
        }

        drawText(font, matrix, bufferSource, displayText, textX, 0.0F, withAlpha(style.glowColor, alpha / 3), Font.DisplayMode.SEE_THROUGH);
        drawOutlinedText(font, matrix, bufferSource, displayText, textX, 0.0F, style, alpha);
        drawGradientText(font, matrix, bufferSource, displayText, textX, 0.0F, style, alpha);
        drawGradientText(font, matrix, bufferSource, displayText, textX, -1.0F, style.highlighted(), alpha / 2);
    }

    private static String localizeReactionText(Minecraft minecraft, String key) {
        LocalizedReactionName name = REACTION_NAMES.get(key);
        if (name == null) {
            return key;
        }

        String language = minecraft.getLanguageManager().getSelected();
        if (language != null && language.toLowerCase(Locale.ROOT).startsWith("zh")) {
            return name.chinese;
        }
        return name.english;
    }

    private static int textAlpha(float lifeProgress) {
        float fadeIn = Mth.clamp(lifeProgress / 0.15F, 0.0F, 1.0F);
        float fadeOut = Mth.clamp((1.0F - lifeProgress) / 0.25F, 0.0F, 1.0F);
        return (int) (255.0F * Math.min(fadeIn, fadeOut));
    }

    private static void drawOutlinedText(
            Font font,
            Matrix4f matrix,
            MultiBufferSource bufferSource,
            String displayText,
            float textX,
            float textY,
            ReactionArtStyle style,
            int alpha
    ) {
        int outlineColor = withAlpha(style.outlineColor, alpha);
        drawText(font, matrix, bufferSource, displayText, textX - 1.0F, textY, outlineColor, Font.DisplayMode.NORMAL);
        drawText(font, matrix, bufferSource, displayText, textX + 1.0F, textY, outlineColor, Font.DisplayMode.NORMAL);
        drawText(font, matrix, bufferSource, displayText, textX, textY - 1.0F, outlineColor, Font.DisplayMode.NORMAL);
        drawText(font, matrix, bufferSource, displayText, textX, textY + 1.0F, outlineColor, Font.DisplayMode.NORMAL);
        drawText(font, matrix, bufferSource, displayText, textX - 1.0F, textY - 1.0F, withAlpha(style.glowColor, alpha / 2), Font.DisplayMode.SEE_THROUGH);
        drawText(font, matrix, bufferSource, displayText, textX + 1.0F, textY + 1.0F, withAlpha(style.glowColor, alpha / 2), Font.DisplayMode.SEE_THROUGH);
    }

    private static void drawGradientText(
            Font font,
            Matrix4f matrix,
            MultiBufferSource bufferSource,
            String displayText,
            float textX,
            float textY,
            ReactionArtStyle style,
            int alpha
    ) {
        int codePointCount = displayText.codePointCount(0, displayText.length());
        float x = textX;
        for (int offset = 0, index = 0; offset < displayText.length(); index++) {
            int codePoint = displayText.codePointAt(offset);
            String glyph = new String(Character.toChars(codePoint));
            float progress = codePointCount <= 1 ? 0.5F : (float) index / (float) (codePointCount - 1);
            int color = withAlpha(lerpColor(style.startColor, style.endColor, progress), alpha);
            drawText(font, matrix, bufferSource, glyph, x, textY, color, Font.DisplayMode.NORMAL);
            x += font.width(glyph);
            offset += Character.charCount(codePoint);
        }
    }

    private static void drawText(
            Font font,
            Matrix4f matrix,
            MultiBufferSource bufferSource,
            String displayText,
            float x,
            float y,
            int color,
            Font.DisplayMode displayMode
    ) {
        font.drawInBatch(
                displayText,
                x,
                y,
                color,
                false,
                matrix,
                bufferSource,
                displayMode,
                0,
                LightTexture.FULL_BRIGHT
        );
    }

    private static int lerpColor(int startColor, int endColor, float progress) {
        int startRed = startColor >> 16 & 255;
        int startGreen = startColor >> 8 & 255;
        int startBlue = startColor & 255;
        int endRed = endColor >> 16 & 255;
        int endGreen = endColor >> 8 & 255;
        int endBlue = endColor & 255;
        int red = Mth.lerpInt(progress, startRed, endRed);
        int green = Mth.lerpInt(progress, startGreen, endGreen);
        int blue = Mth.lerpInt(progress, startBlue, endBlue);
        return red << 16 | green << 8 | blue;
    }

    private static int withAlpha(int color, int alpha) {
        int clampedAlpha = Mth.clamp(alpha, 0, 255);
        if (clampedAlpha > 0 && clampedAlpha < 4) {
            clampedAlpha = 4;
        }
        return clampedAlpha << 24 | color & 0x00FFFFFF;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }

    @Override
    public boolean shouldCull() {
        return false;
    }

    public static final class Provider implements ParticleProvider<ReactionTextParticleOptions> {
        @Override
        public Particle createParticle(
                ReactionTextParticleOptions options,
                ClientLevel level,
                double x,
                double y,
                double z,
                double xSpeed,
                double ySpeed,
                double zSpeed
        ) {
            return new ReactionTextParticle(level, x, y, z, options.text());
        }
    }

    private static final class LocalizedReactionName {
        private final String chinese;
        private final String english;

        private LocalizedReactionName(String chinese, String english) {
            this.chinese = chinese;
            this.english = english;
        }
    }

    private static final class ReactionArtStyle {
        private final int startColor;
        private final int endColor;
        private final int outlineColor;
        private final int glowColor;

        private ReactionArtStyle(int startColor, int endColor, int outlineColor, int glowColor) {
            this.startColor = startColor;
            this.endColor = endColor;
            this.outlineColor = outlineColor;
            this.glowColor = glowColor;
        }

        private ReactionArtStyle highlighted() {
            return new ReactionArtStyle(0xFFFFFF, startColor, outlineColor, glowColor);
        }
    }
}
