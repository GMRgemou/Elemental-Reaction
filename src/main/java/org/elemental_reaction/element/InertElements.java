package org.elemental_reaction.element;

public final class InertElements {
    private InertElements() {
    }

    public static boolean contains(String element) {
        return ElementTypes.isKnownElement(element) && !ActiveElements.contains(element);
    }
}
