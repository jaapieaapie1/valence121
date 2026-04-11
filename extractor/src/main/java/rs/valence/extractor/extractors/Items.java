package rs.valence.extractor.extractors;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import java.util.Optional;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantable;
import rs.valence.extractor.Main;

public class Items implements Main.Extractor {

    private final RegistryAccess.Frozen registryManager;

    public Items(MinecraftServer server) {
        this.registryManager = server.registryAccess();
    }

    @Override
    public String fileName() {
        return "items.json";
    }

    @Override
    public JsonElement extract() throws Exception {
        var itemsJson = new JsonArray();

        for (var item : registryManager
            .lookupOrThrow(Registries.ITEM)
            .listElements()
            .toList()) {
            var itemJson = new JsonObject();

            itemJson.addProperty(
                "id",
                BuiltInRegistries.ITEM.getId(item.value())
            );
            itemJson.addProperty(
                "name",
                item.key().identifier().getPath()
            );
            Item realItem = item.value();
            itemJson.addProperty(
                "translation_key",
                realItem.getDescriptionId()
            );
            itemJson.addProperty("max_stack", realItem.getDefaultInstance().getMaxStackSize());
            itemJson.addProperty(
                "max_durability",
                realItem.getDefaultInstance().getMaxDamage()
            );
            itemJson.addProperty(
                "enchantability",
                Optional.ofNullable(
                    realItem.components().get(DataComponents.ENCHANTABLE)
                )
                    .map(Enchantable::value)
                    .orElse(0)
            );
            itemJson.addProperty(
                "fireproof",
                Optional.ofNullable(
                    realItem
                        .components()
                        .get(DataComponents.DAMAGE_RESISTANT)
                )
                    .map(x -> x.types().unwrapKey().map(t -> t.equals(DamageTypeTags.IS_FIRE)).orElse(false))
                    .orElse(false)
            );

            itemJson.add(
                "components",
                DataComponentMap.CODEC.encodeStart(
                    RegistryOps.create(JsonOps.INSTANCE, registryManager),
                    realItem.components()
                ).getOrThrow()
            );

            itemsJson.add(itemJson);
        }
        return itemsJson;
    }
}
