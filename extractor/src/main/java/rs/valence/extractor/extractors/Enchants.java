package rs.valence.extractor.extractors;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.enchantment.Enchantment;
import rs.valence.extractor.Main;

public class Enchants implements Main.Extractor {

    private final RegistryAccess.Frozen registryManager;

    public Enchants(MinecraftServer server) {
        this.registryManager = server.registryAccess();
    }

    @Override
    public String fileName() {
        return "enchants.json";
    }

    @Override
    public JsonElement extract() {
        var enchantsJson = new JsonObject();

        for (var enchant : registryManager
            .lookupOrThrow(Registries.ENCHANTMENT)
            .listElements()
            .toList()) {
            enchantsJson.add(
                enchant.key().identifier().toString(),
                Enchantment.DIRECT_CODEC.encodeStart(
                    RegistryOps.create(JsonOps.INSTANCE, registryManager),
                    enchant.value()
                ).getOrThrow()
            );
        }

        return enchantsJson;
    }
}
