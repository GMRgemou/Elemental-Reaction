package org.elemental_reaction.element;

import java.util.Optional;

public final class ElementClassifier {
    private ElementClassifier() {
    }

    public static Optional<ElementKind> kindOf(String element) {
        if (ElementTypes.isDefaultElement(element)) {
            return Optional.empty();
        }
        if (InertElements.contains(element)) {
            return Optional.of(ElementKind.INERT);
        }
        if (ActiveElements.contains(element)) {
            return Optional.of(ElementKind.ACTIVE);
        }
        return Optional.empty();
    }
}
