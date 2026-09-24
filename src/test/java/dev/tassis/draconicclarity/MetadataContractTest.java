package dev.tassis.draconicclarity;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class MetadataContractTest {
    @Test
    void metadataKeepsTheIntegrationIndependentAndViewersOptional() throws IOException {
        var resources = Collections.list(getClass().getClassLoader()
            .getResources("META-INF/neoforge.mods.toml"));
        String metadata = null;
        for (var resource : resources) {
            try (InputStream input = resource.openStream()) {
                String candidate = new String(input.readAllBytes(), StandardCharsets.UTF_8);
                if (candidate.contains("modId=\"draconic_clarity\"")) {
                    metadata = candidate;
                    break;
                }
            }
        }

        assertNotNull(metadata);
        assertTrue(metadata.contains("modId=\"draconicevolution\"\ntype=\"required\""));
        assertTrue(metadata.contains("modId=\"jei\"\ntype=\"optional\""));
        assertTrue(metadata.contains("modId=\"jade\"\ntype=\"optional\""));
        assertTrue(metadata.contains("modId=\"patchouli\"\ntype=\"optional\""));
        assertFalse(metadata.contains("modId=\"emi\""));
        assertFalse(metadata.contains("retrodraconic"));
        assertFalse(metadata.contains("modId=\"traveler_echoes\""));
    }

    @Test
    void handbookIsGeneratedAndCraftedFromABookAndDraconiumDust() throws IOException {
        var book = readJson("data/draconic_clarity/patchouli_books/draconic_insight/book.json");
        assertEquals("book.draconic_clarity.name", book.get("name").getAsString());
        assertEquals("patchouli:book_purple", book.get("model").getAsString());
        assertTrue(book.get("use_resource_pack").getAsBoolean());

        var recipe = readJson("data/draconic_clarity/recipe/draconic_insight_book.json");
        assertEquals("minecraft:crafting_shapeless", recipe.get("type").getAsString());
        var condition = recipe.getAsJsonArray("neoforge:conditions").get(0).getAsJsonObject();
        assertEquals("neoforge:mod_loaded", condition.get("type").getAsString());
        assertEquals("patchouli", condition.get("modid").getAsString());
        var ingredients = recipe.getAsJsonArray("ingredients").asList().stream()
            .map(element -> element.getAsJsonObject().get("item").getAsString())
            .toList();
        assertEquals(java.util.List.of("minecraft:book", "draconicevolution:draconium_dust"), ingredients);
        var result = recipe.getAsJsonObject("result");
        assertEquals("patchouli:guide_book", result.get("id").getAsString());
        assertEquals("draconic_clarity:draconic_insight", result.getAsJsonObject("components")
            .get("patchouli:book").getAsString());
    }

    private com.google.gson.JsonObject readJson(String path) throws IOException {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(path)) {
            assertNotNull(input, path);
            return JsonParser.parseString(new String(input.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }
}
