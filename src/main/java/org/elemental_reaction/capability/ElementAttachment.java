package org.elemental_reaction.capability;

import net.minecraft.nbt.CompoundTag;

public class ElementAttachment {
    private static final String ELEMENT_TAG = "Element";
    private static final String EXPIRES_AT_TAG = "ExpiresAt";
    private static final String ELEMENTAL_CALM_UNTIL_TAG = "ElementalCalmUntil";
    private static final String LEGACY_ELEMENT_CALM_UNTIL_TAG = "ElementCalmUntil";

    private String element = "";
    private long expiresAtGameTime;
    private long elementalCalmUntilGameTime;

    public boolean isPresent(long gameTime) {
        return !element.isBlank() && expiresAtGameTime > gameTime;
    }

    public String getElement() {
        return element;
    }

    public long getExpiresAtGameTime() {
        return expiresAtGameTime;
    }

    public boolean isElementalCalm(long gameTime) {
        return elementalCalmUntilGameTime > gameTime;
    }

    public long getElementalCalmUntilGameTime() {
        return elementalCalmUntilGameTime;
    }

    public void attach(String element, long expiresAtGameTime) {
        this.element = element == null ? "" : element;
        this.expiresAtGameTime = expiresAtGameTime;
    }

    public void startElementalCalm(long elementalCalmUntilGameTime) {
        this.elementalCalmUntilGameTime = Math.max(this.elementalCalmUntilGameTime, elementalCalmUntilGameTime);
    }

    public void clear() {
        this.element = "";
        this.expiresAtGameTime = 0L;
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString(ELEMENT_TAG, element);
        tag.putLong(EXPIRES_AT_TAG, expiresAtGameTime);
        tag.putLong(ELEMENTAL_CALM_UNTIL_TAG, elementalCalmUntilGameTime);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        element = tag.getString(ELEMENT_TAG);
        expiresAtGameTime = tag.getLong(EXPIRES_AT_TAG);
        elementalCalmUntilGameTime = tag.contains(ELEMENTAL_CALM_UNTIL_TAG)
                ? tag.getLong(ELEMENTAL_CALM_UNTIL_TAG)
                : tag.getLong(LEGACY_ELEMENT_CALM_UNTIL_TAG);
    }
}
