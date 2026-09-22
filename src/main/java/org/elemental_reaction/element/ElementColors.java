package org.elemental_reaction.element;

import java.util.Map;

public final class ElementColors {
    private static final ElementColor DEFAULT = new ElementColor(1.0F, 1.0F, 1.0F);
    private static final Map<String, ElementColor> COLORS = Map.of(
            "fire", new ElementColor(1.0F, 0.22F, 0.04F),
            "ice", new ElementColor(0.35F, 0.82F, 1.0F),
            "water", new ElementColor(0.12F, 0.42F, 1.0F),
            "thunder", new ElementColor(0.72F, 0.25F, 1.0F),
            "darkness", new ElementColor(0.24F, 0.08F, 0.38F),
            "light", new ElementColor(1.0F, 0.9F, 0.28F),
            "earth", new ElementColor(0.72F, 0.42F, 0.16F),
            "wind", new ElementColor(0.3F, 1.0F, 0.72F),
            "flora", new ElementColor(0.2F, 0.85F, 0.2F)
    );

    private ElementColors() {
    }

    public static ElementColor forElement(String element) {
        return COLORS.getOrDefault(element, DEFAULT);
    }

    public record ElementColor(float red, float green, float blue) {
    }
}
