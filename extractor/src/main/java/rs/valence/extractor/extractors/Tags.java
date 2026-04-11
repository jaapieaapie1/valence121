package rs.valence.extractor.extractors;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;

import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import rs.valence.extractor.Main;
import rs.valence.extractor.RegistryKeyComparator;

public class Tags implements Main.Extractor {

    private final RegistryAccess.Frozen dynamicRegistryManager;

    public Tags(MinecraftServer server) {
        // TODO(26.1): yarn's `server.getCombinedDynamicRegistries()` returned a
        // `CombinedDynamicRegistries<ServerDynamicRegistryType>` which exposed
        // the layered registry stack. In Mojang-mapped 26.1 we use the frozen
        // `RegistryAccess` directly; this is sufficient for tag serialization
        // because tags live on the individual registries.
        this.dynamicRegistryManager = server.registryAccess();
    }

    @Override
    public String fileName() {
        return "tags.json";
    }

    @Override
    public JsonElement extract() {
        var tagsJson = new JsonObject();

        final var registryTags =
            this.dynamicRegistryManager.registries()
                .map(registry ->
                    Pair.of(registry.key(), serializeTags(registry.value()))
                )
                .filter(pair -> !(pair.getSecond()).isEmpty())
                .collect(
                    Collectors.toMap(
                        Pair::getFirst,
                        Pair::getSecond,
                        (l, r) -> r,
                        () -> new TreeMap<>(new RegistryKeyComparator())
                    )
                );

        for (var registry : registryTags.entrySet()) {
            var registryIdent = registry.getKey().identifier().toString();
            var tagGroupTagsJson = new JsonObject();

            for (var tag : registry.getValue().entrySet()) {
                var ident = tag.getKey().toString();
                var rawIds = tag.getValue();
                tagGroupTagsJson.add(ident, rawIds);
            }

            tagsJson.add(registryIdent, tagGroupTagsJson);
        }

        return tagsJson;
    }

    private static <T> Map<Identifier, JsonArray> serializeTags(
        Registry<T> registry
    ) {
        TreeMap<Identifier, JsonArray> map = new TreeMap<>();
        registry
                .getTags()
                .forEach(named -> {
                    var registryEntryList = Lists.newArrayList(named);
                    JsonArray intList = new JsonArray(registryEntryList.size());
                    for (Holder<T> registryEntry : registryEntryList) {
                        if (
                            Holder.Kind.REFERENCE != registryEntry.kind()
                        ) {
                            throw new IllegalStateException(
                                "Can't serialize unregistered value " +
                                registryEntry
                            );
                        }
                        intList.add(registry.getId(registryEntry.value()));
                    }
                    map.put(named.key().location(), intList);
                });
        return map;
    }
}
