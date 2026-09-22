package org.elemental_reaction.element;

import java.util.Set;

public final class ElementTypes {
    public static final String NORMAL = "normal";

    private static final Set<String> KNOWN_ELEMENTAL_COMBAT_ELEMENTS = Set.of(
            "fire",
            "ice",
            "water",
            "thunder",
            "darkness",
            "light",
            "earth",
            "wind",
            "flora"
    );

    private ElementTypes() {
    }

    public static boolean isKnownElement(String element) {
        return element != null && KNOWN_ELEMENTAL_COMBAT_ELEMENTS.contains(element);
    }

    public static boolean isDefaultElement(String element) {
        return element == null || element.isBlank() || NORMAL.equals(element);
    }
}
