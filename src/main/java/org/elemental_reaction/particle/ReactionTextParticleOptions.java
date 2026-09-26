package org.elemental_reaction.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;

public final class ReactionTextParticleOptions implements ParticleOptions {
    public static final ParticleOptions.Deserializer<ReactionTextParticleOptions> DESERIALIZER =
            new ParticleOptions.Deserializer<>() {
                @Override
                public ReactionTextParticleOptions fromCommand(
                        ParticleType<ReactionTextParticleOptions> type,
                        StringReader reader
                ) throws CommandSyntaxException {
                    reader.expect(' ');
                    return new ReactionTextParticleOptions(type, reader.readString());
                }

                @Override
                public ReactionTextParticleOptions fromNetwork(
                        ParticleType<ReactionTextParticleOptions> type,
                        FriendlyByteBuf buffer
                ) {
                    return new ReactionTextParticleOptions(type, buffer.readUtf(64));
                }
            };

    private final ParticleType<ReactionTextParticleOptions> type;
    private final String text;

    public ReactionTextParticleOptions(ParticleType<ReactionTextParticleOptions> type, String text) {
        this.type = type;
        this.text = text;
    }

    public static Codec<ReactionTextParticleOptions> codec(ParticleType<ReactionTextParticleOptions> type) {
        return Codec.STRING.xmap(
                text -> new ReactionTextParticleOptions(type, text),
                ReactionTextParticleOptions::text
        );
    }

    @Override
    public ParticleType<ReactionTextParticleOptions> getType() {
        return type;
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buffer) {
        buffer.writeUtf(text, 64);
    }

    @Override
    public String writeToString() {
        return BuiltInRegistries.PARTICLE_TYPE.getKey(type) + " " + text;
    }

    public String text() {
        return text;
    }
}
