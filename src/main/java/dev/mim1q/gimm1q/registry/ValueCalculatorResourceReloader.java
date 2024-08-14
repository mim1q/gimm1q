package dev.mim1q.gimm1q.registry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dev.mim1q.gimm1q.Gimm1q;
import dev.mim1q.gimm1q.network.s2c.ValueCalculatorSyncS2CPacket;
import dev.mim1q.gimm1q.valuecalculators.internal.ValueCalculatorInternal;
import dev.mim1q.gimm1q.valuecalculators.parameters.ValueCalculatorContext;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import java.io.FileReader;
import java.io.FileWriter;
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
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    public void reload(ResourceManager manager) {
        map.clear();

        var idsToStrings = loadFromDataPacks(manager);
        saveTemplatesToConfigFolder(idsToStrings);
        loadFromConfigFolder(idsToStrings.keySet());
    }

    public void replaceWith(Map<Identifier, List<ValueCalculatorInternal>> map) {
        this.map.clear();
        this.map.putAll(map);
    }

    private Map<Identifier, String> loadFromDataPacks(ResourceManager manager) {
        var idsToStrings = new HashMap<Identifier, String>();

        manager.streamResourcePacks().forEach(pack -> {
            for (var namespace : pack.getNamespaces(ResourceType.SERVER_DATA)) {
                pack.findResources(
                    ResourceType.SERVER_DATA,
                    namespace,
                    "value_calculators",
                    (id, input) -> {
                        // Remove "value_calculators/"
                        var newId = Identifier.of(namespace, id.getPath().substring(18, id.getPath().length() - 5));
                        try {
                            var json = JsonParser.parseReader(new InputStreamReader(input.get()));
                            var internal = ValueCalculatorInternal.CODEC
                                .parse(JsonOps.INSTANCE, json)
                                .result();

                            internal.ifPresent(
                                it -> {
                                    map.computeIfAbsent(newId, k -> new ArrayList<>()).add(it);
                                    idsToStrings.putIfAbsent(newId, GSON.toJson(json));
                                }
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

        return idsToStrings;
    }

    private void saveTemplatesToConfigFolder(Map<Identifier, String> idsToStrings) {
        try {
            var configFolder = FabricLoader.getInstance().getConfigDir().resolve("gimm1q/value_calculator_overrides").toFile();
            if (configFolder.mkdirs()) {
                Gimm1q.LOGGER.info("Created Gimm1q Value Calculator config folder {}", configFolder.getAbsolutePath());
            }

            for (var id : idsToStrings.keySet()) {
                var directory = configFolder.toPath().resolve(id.getNamespace()).toFile();
                var file = directory.toPath().resolve(id.getPath() + ".template.json").toFile();
                if (directory.mkdirs() || directory.createNewFile()) {
                    Gimm1q.LOGGER.info("Created Gimm1q Value Calculator config file {}", file.getAbsolutePath());
                }
                var writer = new FileWriter(file, false);
                writer.write(idsToStrings.get(id));
                writer.close();
            }
        } catch (Throwable e) {
            Gimm1q.LOGGER.error("Failed to create Gimm1q Value Calculator config folder", e);
        }
    }

    private void loadFromConfigFolder(Set<Identifier> ids) {
        try {
            var configFolder = FabricLoader.getInstance().getConfigDir().resolve("gimm1q/value_calculator_overrides").toFile();
            if (!configFolder.exists()) {
                return;
            }

            for (var id : ids) {
                var file = configFolder.toPath().resolve(id.getNamespace() + "/" + id.getPath() + ".json").toFile();
                if (!file.exists()) {
                    continue;
                }
                var internal = ValueCalculatorInternal.CODEC
                    .parse(JsonOps.INSTANCE, JsonParser.parseReader(new FileReader(file)))
                    .result();
                internal.ifPresent(it -> map.computeIfAbsent(id, k -> new ArrayList<>()).add(it));
            }

        } catch (Throwable e) {
            Gimm1q.LOGGER.error("Failed to load Gimm1q Value Calculator config folder", e);
        }
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

        for (var i = list.size() - 1; i >= 0; i--) {
            var it = list.get(i);
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

    public ValueCalculatorSyncS2CPacket createPacket() {
        return new ValueCalculatorSyncS2CPacket(map);
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
