package rs.valence.extractor.extractors;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import rs.valence.extractor.Main;

public class Effects implements Main.Extractor {

    public Effects() {}

    @Override
    public String fileName() {
        return "effects.json";
    }

    @Override
    public JsonElement extract() {
        var effectsJson = new JsonArray();

        for (var effect : BuiltInRegistries.MOB_EFFECT) {
            var effectJson = new JsonObject();

            effectJson.addProperty(
                "id",
                BuiltInRegistries.MOB_EFFECT.getId(effect)
            );
            effectJson.addProperty(
                "name",
                BuiltInRegistries.MOB_EFFECT.getKey(effect).getPath()
            );
            effectJson.addProperty(
                "translation_key",
                effect.getDescriptionId()
            );
            effectJson.addProperty("color", effect.getColor());
            effectJson.addProperty("instant", effect.isInstantenous());
            effectJson.addProperty("category", effect.getCategory().name());

            var attributeModifiersJson = new JsonArray();

            // TODO(26.1): MobEffect#forEachAttributeModifier was removed; the
            // replacement API (MobEffect$AttributeTemplate / createModifiers)
            // could not be verified without javap access. Re-enable once the
            // correct iteration API is confirmed against the 26.1.2 jar.
            // Stubbed with an empty loop to keep the file compiling.
            if (false) {
                // unreachable placeholder to preserve the JsonObject shape
                var attributeModifierJson = new JsonObject();
                attributeModifierJson.addProperty("attribute_name", "");
                attributeModifierJson.addProperty("operation", 0);
                attributeModifierJson.addProperty("base_value", 0.0);
                attributeModifiersJson.add(attributeModifierJson);
            }

            if (attributeModifiersJson.size() > 0) {
                effectJson.add("attribute_modifiers", attributeModifiersJson);
            }

            effectsJson.add(effectJson);
        }

        return effectsJson;
    }
}
