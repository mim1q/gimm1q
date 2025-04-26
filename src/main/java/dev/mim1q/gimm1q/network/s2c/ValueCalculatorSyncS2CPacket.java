package dev.mim1q.gimm1q.network.s2c;

import dev.mim1q.gimm1q.Gimm1q;
import dev.mim1q.gimm1q.valuecalculators.internal.ValueCalculatorInternal;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record ValueCalculatorSyncS2CPacket(
    Map<Identifier, List<ValueCalculatorInternal>> map
) implements CustomPayload {
    public static final Id<ValueCalculatorSyncS2CPacket> ID = new Id<>(Gimm1q.id("value_calculator_sync"));
    public static final PacketCodec<RegistryByteBuf, ValueCalculatorSyncS2CPacket> CODEC = PacketCodec.of(
        (packet, buf) -> Data.writeBuf(buf, packet.map()),
        (buf) -> new ValueCalculatorSyncS2CPacket(Data.readMap(buf))
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    private static class Data {
        public static void writeBuf(RegistryByteBuf buf, Map<Identifier, List<ValueCalculatorInternal>> map) {
            buf.writeInt(map.size());

            for (var entry : map.entrySet()) {
                buf.writeIdentifier(entry.getKey());
                var nbts = new ArrayList<NbtCompound>();

                for (ValueCalculatorInternal calculator : entry.getValue()) {
                    try {
                        nbts.add((NbtCompound) ValueCalculatorInternal.CODEC
                            .encodeStart(NbtOps.INSTANCE, calculator)
                            .getOrThrow(e -> new RuntimeException(String.format(
                                "Failed to encode Value Calculator %s to send to client. %s", entry.getKey(), e
                            )))
                        );
                    } catch (Exception e) {
                        Gimm1q.LOGGER.warn("Failed to encode Value Calculator {} to send to client. {}", entry.getKey(), e);
                    }
                }

                buf.writeInt(nbts.size());
                for (NbtCompound nbt : nbts) {
                    buf.writeNbt(nbt);
                }
            }
        }

        public static Map<Identifier, List<ValueCalculatorInternal>> readMap(PacketByteBuf buffer) {
            var mapSize = buffer.readInt();

            var map = new HashMap<Identifier, List<ValueCalculatorInternal>>();
            for (int i = 0; i < mapSize; ++i) {
                var id = buffer.readIdentifier();
                var listSize = buffer.readInt();

                var list = new ArrayList<ValueCalculatorInternal>();
                for (int j = 0; j < listSize; ++j) {
                    var nbt = buffer.readNbt();
                    if (nbt == null) continue;
                    list.add(ValueCalculatorInternal.CODEC
                        .parse(NbtOps.INSTANCE, nbt)
                        .result()
                        .orElseThrow(() -> new IllegalStateException("Failed to decode Value Calculator from client."))
                    );
                }

                map.put(id, list);
            }

            return map;
        }
    }
}
