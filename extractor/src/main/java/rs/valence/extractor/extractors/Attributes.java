package rs.valence.extractor.extractors;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import rs.valence.extractor.Main;

public class Attributes implements Main.Extractor {

    public Attributes() {}

    @Override
    public String fileName() {
        return "attributes.json";
    }

    @Override
    public JsonElement extract() {
        var attributesJson = new JsonObject();

        for (Attribute attribute : BuiltInRegistries.ATTRIBUTE) {
            var attributeJson = new JsonObject();

            attributeJson.addProperty(
                "id",
                BuiltInRegistries.ATTRIBUTE.getId(attribute)
            );
            attributeJson.addProperty(
                "name",
                BuiltInRegistries.ATTRIBUTE.getKey(attribute).getPath()
            );
            attributeJson.addProperty(
                "default_value",
                attribute.getDefaultValue()
            );
            attributeJson.addProperty(
                "translation_key",
                attribute.getDescriptionId()
            );
            // TODO(26.1): Attribute.isTracked() (yarn) has no verified Mojang
            // equivalent here; stubbed to false until confirmed via javap.
            attributeJson.addProperty("tracked", false);

            if (attribute instanceof RangedAttribute a) {
                attributeJson.addProperty("min_value", a.getMinValue());
                attributeJson.addProperty("max_value", a.getMaxValue());
            }

            attributesJson.add(
                BuiltInRegistries.ATTRIBUTE.getKey(attribute).getPath(),
                attributeJson
            );
        }

        return attributesJson;
    }
}
