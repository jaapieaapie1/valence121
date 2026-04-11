package rs.valence.extractor.extractors;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;
import rs.valence.extractor.Main;

public class Paintings implements Main.Extractor {

    private final RegistryAccess.Frozen registryManager;

    public Paintings(MinecraftServer server) {
        this.registryManager = server.registryAccess();
    }

    @Override
    public String fileName() {
        return "paintings.json";
    }

    @Override
    public JsonElement extract() throws Exception {
        var paintingRegistry = registryManager.lookupOrThrow(
            Registries.PAINTING_VARIANT
        );

        // TODO(26.1): verify PaintingVariant.DIRECT_CODEC is the correct codec
        // field name in Mojang mappings (Enchantment uses DIRECT_CODEC as well).
        var codec = PaintingVariant.DIRECT_CODEC;

        JsonObject json = new JsonObject();
        paintingRegistry
            .listElements()
            .forEach(entry -> {
                json.add(
                    entry.key().identifier().toString(),
                    codec
                        .encodeStart(
                            RegistryOps.create(JsonOps.INSTANCE, registryManager),
                            entry.value()
                        )
                        .getOrThrow()
                );
            });

        return json;
    }
}
