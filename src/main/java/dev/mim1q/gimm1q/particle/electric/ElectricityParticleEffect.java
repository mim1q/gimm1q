package dev.mim1q.gimm1q.particle.electric;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.mim1q.gimm1q.registry.Gimm1qParticles;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.math.Vec3d;

public record ElectricityParticleEffect(
    Vec3d direction,
    int length,
    int color,
    float size,
    boolean isMainBranch
) implements ParticleEffect {
    @SuppressWarnings("deprecation")
    public static final ParticleEffect.Factory<ElectricityParticleEffect> PARAMETERS_FACTORY = new ParticleEffect.Factory<>() {
        @Override
        public ElectricityParticleEffect read(
            ParticleType<ElectricityParticleEffect> type,
            StringReader stringReader
        ) throws CommandSyntaxException {
            stringReader.expect(' ');
            var x = stringReader.readDouble();
            stringReader.expect(' ');
            var y = stringReader.readDouble();
            stringReader.expect(' ');
            var z = stringReader.readDouble();
            stringReader.expect(' ');
            var length = stringReader.readInt();
            stringReader.expect(' ');
            var color = stringReader.readInt();
            stringReader.expect(' ');
            var size = stringReader.readFloat();
            stringReader.expect(' ');
            var isMainBranch = stringReader.readBoolean();
            return new ElectricityParticleEffect(new Vec3d(x, y, z), length, color, size, isMainBranch);
        }

        @Override
        public ElectricityParticleEffect read(
            ParticleType<ElectricityParticleEffect> type,
            PacketByteBuf buf
        ) {
            var x = buf.readDouble();
            var y = buf.readDouble();
            var z = buf.readDouble();
            var length = buf.readInt();
            var color = buf.readInt();
            var size = buf.readFloat();
            var isMainBranch = buf.readBoolean();
            return new ElectricityParticleEffect(new Vec3d(x, y, z), length, color, size, isMainBranch);
        }
    };

    public static final Codec<ElectricityParticleEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Vec3d.CODEC.fieldOf("direction").forGetter(ElectricityParticleEffect::direction),
        Codec.INT.fieldOf("length").forGetter(ElectricityParticleEffect::length),
        Codec.INT.fieldOf("color").forGetter(ElectricityParticleEffect::color),
        Codec.FLOAT.fieldOf("size").forGetter(ElectricityParticleEffect::size),
        Codec.BOOL.fieldOf("isMainBranch").forGetter(ElectricityParticleEffect::isMainBranch)
    ).apply(instance, ElectricityParticleEffect::new));

    @Override
    public ParticleType<?> getType() {
        return Gimm1qParticles.ELECTRICITY;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeDouble(this.direction.x);
        buf.writeDouble(this.direction.y);
        buf.writeDouble(this.direction.z);
        buf.writeInt(this.length);
        buf.writeInt(this.color);
        buf.writeFloat(this.size);
        buf.writeBoolean(this.isMainBranch);
    }

    @Override
    public String asString() {
        return String.format(
            "electricity{direction=%s, length=%s, color=%s, size=%s, isMainBranch=%s}",
            this.direction,
            this.length,
            this.color,
            this.size,
            this.isMainBranch
        );
    }
}
