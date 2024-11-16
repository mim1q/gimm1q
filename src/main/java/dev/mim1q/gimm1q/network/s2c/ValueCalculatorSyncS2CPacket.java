package dev.mim1q.gimm1q.network.s2c;

import dev.mim1q.gimm1q.Gimm1q;
import dev.mim1q.gimm1q.valuecalculators.internal.ValueCalculatorInternal;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValueCalculatorSyncS2CPacket extends PacketByteBuf {
    public ValueCalculatorSyncS2CPacket(Map<Identifier, List<ValueCalculatorInternal>> map) {
        super(Unpooled.buffer());
        writeInt(map.size());

        for (var entry : map.entrySet()) {
            writeIdentifier(entry.getKey());
            var nbts = new ArrayList<NbtCompound>();

            for (ValueCalculatorInternal calculator : entry.getValue()) {
                try {
                    nbts.add((NbtCompound) ValueCalculatorInternal.CODEC
                        .encodeStart(NbtOps.INSTANCE, calculator)
                        .getOrThrow(true, e -> {
                            Gimm1q.LOGGER.error("Failed to encode Value Calculator {} to send to client. {}", entry.getKey(), e);
                        })
                    );
                } catch (Exception e) {
                    Gimm1q.LOGGER.warn("Failed to encode Value Calculator {} to send to client. {}", entry.getKey(), e);
                }
            }

            writeInt(nbts.size());
            for (NbtCompound nbt : nbts) {
                writeNbt(nbt);
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
