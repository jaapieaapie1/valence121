package rs.valence.extractor.extractors;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.crafting.RecipeManager;
import rs.valence.extractor.Main;

public class Recipe implements Main.Extractor {

    private final RegistryAccess.Frozen registryManager;
    private final RecipeManager recipeManager;

    public Recipe(MinecraftServer server) {
        this.registryManager = server.registryAccess();
        // TODO(26.1): server.getRecipeManager() no longer exists; RecipeManager
        // is now obtained via a ReloadableServerRegistries / ServerResources
        // path. Stubbed as null until the new access path is wired up.
        this.recipeManager = null;
    }

    @Override
    public String fileName() {
        return "recipes.json";
    }

    @Override
    public JsonElement extract() throws Exception {
        JsonObject json = new JsonObject();

        // TODO(26.1): The entire recipe extraction needs to be rewritten for
        // the new Recipe API:
        //   - `Registries.RECIPE_SERIALIZER.getCodec()` is replaced by
        //     `BuiltInRegistries.RECIPE_SERIALIZER.byNameCodec()` (or
        //     equivalent), and `RecipeSerializer::codec` was removed (codecs
        //     now live on the serializer differently).
        //   - `ServerRecipeManager#values()` returning entries with
        //     `.id().getValue()` no longer exists; recipes are now iterated
        //     through `RecipeManager#getRecipes()` returning
        //     `RecipeHolder<?>` objects.
        //   - `RegistryKeys.RECIPE_DISPLAY` / `RECIPE_BOOK_CATEGORY` are now
        //     `net.minecraft.core.registries.Registries.RECIPE_DISPLAY` etc.,
        //     but the display/book-category registries have moved under
        //     `net.minecraft.world.item.crafting.display` and their access
        //     pattern (`getCodec`, `streamEntries`, `getRawId`) has changed.
        // Stubbed with empty JSON objects so the extractor still compiles and
        // runs for other data sources.
        JsonObject recipesJson = new JsonObject();
        JsonObject displaysJson = new JsonObject();
        JsonObject bookCategoryJson = new JsonObject();

        // Suppress "unused field" warnings while the TODO is outstanding.
        if (this.registryManager == null || this.recipeManager == null) {
            // intentionally empty
        }

        json.add("recipes", recipesJson);
        json.add("displays", displaysJson);
        json.add("book_categories", bookCategoryJson);

        return json;
    }
}
