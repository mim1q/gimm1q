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
            writeInt(entry.getValue().size());

            for (ValueCalculatorInternal calculator : entry.getValue()) {
                writeNbt((NbtCompound) ValueCalculatorInternal.CODEC
                    .encode(calculator, NbtOps.INSTANCE, new NbtCompound())
                    .getOrThrow(true, e -> {
                        Gimm1q.LOGGER.error("Failed to encode Value Calculator {} to send to client. {}", entry.getKey(), e);
                    })
                );
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
                list.add(ValueCalculatorInternal.CODEC
                    .parse(NbtOps.INSTANCE, buffer.readNbt())
                    .result()
                    .orElseThrow(() -> new IllegalStateException("Failed to decode Value Calculator from client."))
                );
            }

            map.put(id, list);
        }

        return map;
    }
}
