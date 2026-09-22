package org.elemental_reaction.element;

import java.util.Set;

public final class ActiveElements {
    private static final Set<String> ELEMENTS = Set.of(
            "wind",
            "thunder",
            "light"
    );

    private ActiveElements() {
    }

    public static boolean contains(String element) {
        return element != null && ELEMENTS.contains(element);
    }
}
