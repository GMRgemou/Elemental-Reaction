package org.elemental_reaction.particle;

import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleType;

public final class ReactionTextParticleType extends ParticleType<ReactionTextParticleOptions> {
    public ReactionTextParticleType() {
        super(true, ReactionTextParticleOptions.DESERIALIZER);
    }

    @Override
    public Codec<ReactionTextParticleOptions> codec() {
        return ReactionTextParticleOptions.codec(this);
    }
}
