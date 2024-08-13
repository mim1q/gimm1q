package dev.mim1q.gimm1q.registry;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dev.mim1q.gimm1q.Gimm1q;
import dev.mim1q.gimm1q.valuecalculators.internal.ValueCalculatorInternal;
import dev.mim1q.gimm1q.valuecalculators.parameters.ValueCalculatorContext;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ValueCalculatorResourceReloader implements SimpleSynchronousResourceReloadListener {
    private ValueCalculatorResourceReloader() {
    }

    public static final ValueCalculatorResourceReloader INSTANCE = new ValueCalculatorResourceReloader();

    private static final Identifier ID = Gimm1q.id("value_calculators");
    private final ConcurrentHashMap<Identifier, List<ValueCalculatorInternal>> map = new ConcurrentHashMap<>();

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    public void reload(ResourceManager manager) {
        map.clear();

        manager.streamResourcePacks().forEach(pack -> {
            for (var namespace : pack.getNamespaces(ResourceType.SERVER_DATA)) {
                pack.findResources(
                    ResourceType.SERVER_DATA,
                    namespace,
                    "value_calculators",
                    (id, input) -> {
                        var newId = Identifier.of(namespace, id.getPath().substring(18, id.getPath().length() - 5));
                        try {
                            var internal = ValueCalculatorInternal.CODEC
                                .parse(JsonOps.INSTANCE, JsonParser.parseReader(new InputStreamReader(input.get())))
                                .result();

                            internal.ifPresent(
                                it -> map.computeIfAbsent(newId, k -> new ArrayList<>()).add(it)
                            );
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        } catch (IllegalStateException e) {
                            throw new RuntimeException("Failed to create value calculator: " + newId, e);
                        }
                    }
                );
            }
        });
    }

    public Optional<Double> calculateExpressionOrVariable(
        Identifier id,
        String name,
        ValueCalculatorContext context,
        boolean variable
    ) {
        var list = map.get(id);
        if (list == null) {
            Gimm1q.LOGGER.error("Value Calculator file not found: {}. Defaulting to 0.0", id);
            return Optional.empty();
        }

        for (var it : list) {
            var exp = variable
                ? it.tryCalculateVariable(name, context)
                : it.tryCalculateExpression(name, context);
            if (exp.isPresent()) {
                return exp;
            }
        }

        Gimm1q.LOGGER.error("Value Calculator expression or variable not found: {}.{}. Defaulting to 0.0", id, name);
        return Optional.empty();
    }

    public Optional<Double> calculateExpression(Identifier id, String name, ValueCalculatorContext context) {
        return calculateExpressionOrVariable(id, name, context, false);
    }

    public Optional<Double> calculateVariable(Identifier id, String name, ValueCalculatorContext context) {
        return calculateExpressionOrVariable(id, name, context, true);
    }

    public static Set<Identifier> getAllIds() {
        return INSTANCE.map.keySet();
    }

    public static Optional<Set<String>> getExpressionOrVariableNames(Identifier id, boolean variables) {
        var list = INSTANCE.map.get(id);
        if (list == null) {
            return Optional.empty();
        }
        return Optional.of(list.stream().reduce(
            new HashSet<>(),
            (acc, it) -> {
                acc.addAll(variables ? it.getVariableNames() : it.getExpressionNames());
                return acc;
            },
            (a, b) -> {
                a.addAll(b);
                return a;
            }
        ));
    }
}
