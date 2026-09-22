package org.elemental_reaction.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@AutoRegisterCapability
public class ElementAttachmentCapability implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static final Capability<ElementAttachment> INSTANCE = CapabilityManager.get(new CapabilityToken<>() {
    });

    private final ElementAttachment attachment = new ElementAttachment();
    private final LazyOptional<ElementAttachment> optional = LazyOptional.of(() -> attachment);

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        return capability == INSTANCE ? optional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return attachment.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        attachment.deserializeNBT(tag);
    }
}
