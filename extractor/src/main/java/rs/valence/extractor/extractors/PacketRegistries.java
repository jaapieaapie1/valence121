package rs.valence.extractor.extractors;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.util.stream.Stream;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import rs.valence.extractor.Main;

public class PacketRegistries implements Main.Extractor {

    private final RegistryAccess.Frozen registryManager;
    private final LayeredRegistryAccess<RegistryLayer> registries;

    public PacketRegistries(MinecraftServer server) {
        this.registryManager = server.registryAccess();
        // TODO(26.1): yarn server.getCombinedDynamicRegistries() maps to Mojang
        // server.registries() in 1.21.x; not verified for 26.1.2 (javap unavailable
        // in sandbox). Revisit if the accessor name differs.
        this.registries = server.registries();
    }

    public String fileName() {
        return "registry_codec.json";
    }

    public static <T> JsonObject mapJson(
        RegistryDataLoader.RegistryData<T> registry_entry,
        RegistryAccess.Frozen registryManager,
        LayeredRegistryAccess<RegistryLayer> combinedRegistries
    ) {
        Codec<T> codec = registry_entry.elementCodec();
        Registry<T> registry = registryManager.lookupOrThrow(registry_entry.key());
        JsonObject json = new JsonObject();
        registry
            .listElements()
            .forEach(entry -> {
                json.add(
                    entry.key().identifier().toString(),
                    codec
                        .encodeStart(
                            // TODO(26.1): yarn
                            // combinedRegistries.getCombinedRegistryManager().getOps(JsonOps.INSTANCE)
                            // used the layered composite. registryManager from
                            // server.registryAccess() is already the composite
                            // frozen access in 1.21.x; not verified for 26.1.2
                            // (javap unavailable in sandbox).
                            RegistryOps.create(
                                JsonOps.INSTANCE,
                                registryManager
                            ),
                            entry.value()
                        )
                        .resultOrPartial(e ->
                            Main.LOGGER.error("Cannot encode json: {}", e)
                        )
                        .orElseThrow()
                );
            });
        return json;
    }

    public JsonElement extract() {
        // TODO(26.1): yarn RegistryLoader.SYNCED_REGISTRIES likely maps to
        // Mojang RegistryDataLoader.SYNCHRONIZED_REGISTRIES in 1.21.x; not
        // verified for 26.1.2 (javap unavailable in sandbox). If the field
        // name or list type differs, adjust accordingly.
        Stream<RegistryDataLoader.RegistryData<?>> registries =
            RegistryDataLoader.SYNCHRONIZED_REGISTRIES.stream();
        JsonObject json = new JsonObject();
        registries.forEach(entry -> {
            json.add(
                entry.key().identifier().toString(),
                mapJson(entry, registryManager, this.registries)
            );
        });
        return json;
    }
}
