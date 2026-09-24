package dev.tassis.draconicinsight;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class HandbookResourceTest {
    private static final String ROOT = "assets/draconic_insight/patchouli_books/draconic_insight/";
    private static final List<String> CATEGORIES = List.of(
        "getting_started", "energy_networks", "energy_storage", "fusion_crafting", "equipment", "reactor"
    );
    private static final List<String> ENTRIES = List.of(
        "getting_started/overview",
        "getting_started/crystal_binder",
        "energy_networks/relay_crystals",
        "energy_networks/direct_io",
        "energy_networks/wireless",
        "energy_storage/energy_core",
        "energy_storage/stabilizers_and_pylons",
        "fusion_crafting/setup",
        "fusion_crafting/crafting",
        "equipment/progression",
        "equipment/modular_equipment",
        "equipment/modules",
        "reactor/setup",
        "reactor/operation"
    );

    @Test
    void englishAndTraditionalChineseHaveMatchingReadableContent() throws IOException {
        for (String category : CATEGORIES) {
            JsonObject english = read(ROOT + "en_us/categories/" + category + ".json");
            JsonObject traditionalChinese = read(ROOT + "zh_tw/categories/" + category + ".json");
            assertEquals(english.keySet(), traditionalChinese.keySet(), category);
            assertTrue(english.has("name") && english.has("description") && english.has("icon"), category);
        }

        for (String entry : ENTRIES) {
            JsonObject english = read(ROOT + "en_us/entries/" + entry + ".json");
            JsonObject traditionalChinese = read(ROOT + "zh_tw/entries/" + entry + ".json");
            assertEquals(english.get("category"), traditionalChinese.get("category"), entry);
            assertEquals(pageTypes(english), pageTypes(traditionalChinese), entry);
            assertTrue(english.getAsJsonArray("pages").size() > 0, entry);
        }
    }

    @Test
    void allReferencedDraconicEvolutionRecipesExist() throws IOException {
        for (String entry : ENTRIES) {
            JsonArray pages = read(ROOT + "en_us/entries/" + entry + ".json").getAsJsonArray("pages");
            for (var pageElement : pages) {
                JsonObject page = pageElement.getAsJsonObject();
                assertRecipeExists(page, "recipe");
                assertRecipeExists(page, "recipe2");
            }
        }
    }

    @Test
    void multiblocksHaveOneAnchorAndMapEveryVisibleSymbol() throws IOException {
        for (String entry : ENTRIES) {
            JsonArray pages = read(ROOT + "en_us/entries/" + entry + ".json").getAsJsonArray("pages");
            for (var pageElement : pages) {
                JsonObject page = pageElement.getAsJsonObject();
                if (!"patchouli:multiblock".equals(page.get("type").getAsString())) continue;
                JsonObject multiblock = page.getAsJsonObject("multiblock");
                JsonArray pattern = multiblock.getAsJsonArray("pattern");
                JsonObject mapping = multiblock.getAsJsonObject("mapping");
                int anchors = 0;
                int rows = pattern.get(0).getAsJsonArray().size();
                int columns = pattern.get(0).getAsJsonArray().get(0).getAsString().length();
                for (var layerElement : pattern) {
                    JsonArray layer = layerElement.getAsJsonArray();
                    assertEquals(rows, layer.size(), entry);
                    for (var rowElement : layer) {
                        String row = rowElement.getAsString();
                        assertEquals(columns, row.length(), entry);
                        for (char symbol : row.toCharArray()) {
                            if (symbol == '0') anchors++;
                            else if (!Set.of(' ', '_').contains(symbol)) {
                                assertTrue(mapping.has(Character.toString(symbol)), entry + ": " + symbol);
                            }
                        }
                    }
                }
                assertEquals(1, anchors, entry);
            }
        }

        JsonObject fusion = read(ROOT + "en_us/entries/fusion_crafting/setup.json");
        JsonObject fusionMapping = fusion.getAsJsonArray("pages").get(2).getAsJsonObject()
            .getAsJsonObject("multiblock").getAsJsonObject("mapping");
        assertEquals("minecraft:purple_stained_glass", fusionMapping.get("C").getAsString());
    }

    @Test
    void earlyEnergyCoreDiagramsMatchLockedStructures() throws IOException {
        JsonArray pages = read(ROOT + "en_us/entries/energy_storage/energy_core.json").getAsJsonArray("pages");
        assertSymbolCounts(pageNamed(pages, "Tier 1 Core"), Map.of('X', 1));
        assertSymbolCounts(pageNamed(pages, "Tier 2 Core"), Map.of('D', 6, 'X', 1));
        assertSymbolCounts(pageNamed(pages, "Tier 3 Core"), Map.of('D', 26, 'X', 1));
        assertSymbolCounts(pageNamed(pages, "Tier 4 Core"), Map.of('D', 54, 'R', 26, 'X', 1));
    }

    @Test
    void specialRenderedControllersUseSafeDiagramMarkers() throws IOException {
        JsonArray energyPages = read(ROOT + "en_us/entries/energy_storage/energy_core.json").getAsJsonArray("pages");
        for (int tier = 1; tier <= 4; tier++) {
            assertSafeProxy(pageNamed(energyPages, "Tier " + tier + " Core"), "X");
        }

        JsonArray fusionPages = read(ROOT + "en_us/entries/fusion_crafting/setup.json").getAsJsonArray("pages");
        assertSafeProxy(pageNamed(fusionPages, "Example Four-Injector Layout"), "C");

        JsonArray reactorPages = read(ROOT + "en_us/entries/reactor/setup.json").getAsJsonArray("pages");
        assertSafeProxy(pageNamed(reactorPages, "Four Stabilizers"), "C");
        assertSafeProxy(pageNamed(reactorPages, "Vertical Injector Axis"), "C");
        assertVerticalReactorInjectorAxis(pageNamed(reactorPages, "Vertical Injector Axis"));
    }

    @Test
    void patchouliCraftingPagesNeverReferenceFusionRecipes() throws IOException {
        for (String locale : List.of("en_us", "zh_tw")) {
            for (String entry : ENTRIES) {
                JsonArray pages = read(ROOT + locale + "/entries/" + entry + ".json").getAsJsonArray("pages");
                for (var pageElement : pages) {
                    JsonObject page = pageElement.getAsJsonObject();
                    if (!"patchouli:crafting".equals(page.get("type").getAsString())) continue;
                    assertNotFusionRecipe(page, "recipe");
                    assertNotFusionRecipe(page, "recipe2");
                }
            }
        }
    }

    @Test
    void handbookDoesNotAssumeOptionalViewerMods() throws IOException {
        for (String locale : List.of("en_us", "zh_tw")) {
            for (String category : CATEGORIES) {
                assertNoViewerNames(read(ROOT + locale + "/categories/" + category + ".json"), category);
            }
            for (String entry : ENTRIES) {
                assertNoViewerNames(read(ROOT + locale + "/entries/" + entry + ".json"), entry);
            }
        }
    }

    @Test
    void energyCoreWalkthroughCoversACompleteWorkingBuild() throws IOException {
        JsonArray english = read(ROOT + "en_us/entries/energy_storage/energy_core.json").getAsJsonArray("pages");
        String instructions = narrativeText(english);
        for (String required : List.of(
            "Tier 1 needs no shell",
            "carry every required shell block",
            "places the shell automatically",
            "Assemble Core builds only",
            "Use $(bold)Build Guide$() only",
            "1–15 blocks",
            "Core Invalid",
            "Stabilizers Invalid",
            "Energy Pylon",
            "not the controller"
        )) {
            assertTrue(instructions.contains(required), required);
        }

        JsonObject stabilizers = pageNamed(english, "Tier 1–4 Stabilizers");
        assertSymbolCounts(stabilizers, Map.of('S', 4, 'X', 1));
        assertSafeProxy(stabilizers, "X");

        JsonArray stabilizerEntry = read(ROOT + "en_us/entries/energy_storage/stabilizers_and_pylons.json")
            .getAsJsonArray("pages");
        JsonObject advancedStabilizer = pageNamed(stabilizerEntry, "Advanced Stabilizer");
        assertSymbolCounts(advancedStabilizer, Map.of('S', 9));
        assertTrue(narrativeText(stabilizerEntry).contains("36 stabilizer blocks total"));
    }

    @Test
    void handbookUsesRestrainedNarrationAndRecommendedChineseTitle() throws IOException {
        JsonObject traditionalChineseLang = read("assets/draconic_insight/lang/zh_tw.json");
        assertEquals("龍之洞察", traditionalChineseLang.get("book.draconic_insight.name").getAsString());

        for (String locale : List.of("en_us", "zh_tw")) {
            for (String category : CATEGORIES) {
                assertRestrainedNarration(read(ROOT + locale + "/categories/" + category + ".json"), locale, category);
            }
            for (String entry : ENTRIES) {
                assertRestrainedNarration(read(ROOT + locale + "/entries/" + entry + ".json"), locale, entry);
            }
        }
    }

    @Test
    void handbookReadsAsStandaloneModDocumentation() throws IOException {
        for (String locale : List.of("en_us", "zh_tw")) {
            for (String category : CATEGORIES) {
                assertNoDeploymentFraming(read(ROOT + locale + "/categories/" + category + ".json"), locale, category);
            }
            for (String entry : ENTRIES) {
                assertNoDeploymentFraming(read(ROOT + locale + "/entries/" + entry + ".json"), locale, entry);
            }
        }
    }

    private JsonObject pageNamed(JsonArray pages, String name) {
        return pages.asList().stream()
            .map(JsonElement::getAsJsonObject)
            .filter(page -> page.has("name") && name.equals(page.get("name").getAsString()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Missing page: " + name));
    }

    private String narrativeText(JsonArray pages) {
        StringBuilder text = new StringBuilder();
        for (var pageElement : pages) {
            JsonObject page = pageElement.getAsJsonObject();
            for (String key : List.of("name", "title", "text")) {
                if (page.has(key)) text.append(page.get(key).getAsString()).append('\n');
            }
        }
        return text.toString();
    }

    private void assertRestrainedNarration(JsonElement element, String locale, String path) {
        if (element.isJsonArray()) {
            element.getAsJsonArray().forEach(child -> assertRestrainedNarration(child, locale, path));
            return;
        }
        if (!element.isJsonObject()) return;

        JsonObject object = element.getAsJsonObject();
        for (String key : List.of("name", "title", "text", "description")) {
            if (!object.has(key) || !object.get(key).isJsonPrimitive()) continue;
            String text = object.get(key).getAsString();
            if ("zh_tw".equals(locale)) {
                assertFalse(text.contains("我"), path + ": " + text);
            } else {
                assertFalse(text.matches("(?is).*(?:^|\\s)(?:i|my|me|mine)(?:\\s|[.,:;!?—]|$).*"), path + ": " + text);
            }
        }
        object.entrySet().forEach(entry -> assertRestrainedNarration(entry.getValue(), locale, path));
    }

    private void assertNoDeploymentFraming(JsonElement element, String locale, String path) {
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            String text = element.getAsString();
            if ("zh_tw".equals(locale)) {
                assertFalse(text.matches(".*(?:整合包|安裝環境|實際安裝|目前安裝).*"), path + ": " + text);
            } else {
                assertFalse(text.matches("(?is).*\\b(?:modpack|pack|installation|installed)\\b.*"), path + ": " + text);
            }
        } else if (element.isJsonArray()) {
            element.getAsJsonArray().forEach(child -> assertNoDeploymentFraming(child, locale, path));
        } else if (element.isJsonObject()) {
            element.getAsJsonObject().entrySet().forEach(entry -> assertNoDeploymentFraming(entry.getValue(), locale, path));
        }
    }

    private void assertSymbolCounts(JsonObject page, Map<Character, Integer> expected) {
        Map<Character, Integer> actual = new java.util.HashMap<>();
        JsonArray pattern = page.getAsJsonObject("multiblock").getAsJsonArray("pattern");
        for (var layerElement : pattern) {
            for (var rowElement : layerElement.getAsJsonArray()) {
                for (char symbol : rowElement.getAsString().toCharArray()) {
                    if (symbol != '0' && symbol != ' ' && symbol != '_') {
                        actual.merge(symbol, 1, Integer::sum);
                    }
                }
            }
        }
        assertEquals(expected, actual, page.get("name").getAsString());
    }

    private void assertVerticalReactorInjectorAxis(JsonObject page) {
        JsonArray pattern = page.getAsJsonObject("multiblock").getAsJsonArray("pattern");
        assertEquals(6, pattern.size());
        assertEquals("C", pattern.get(1).getAsJsonArray().get(0).getAsString());
        assertEquals("I", pattern.get(5).getAsJsonArray().get(0).getAsString());
    }

    private void assertSafeProxy(JsonObject page, String symbol) {
        assertFalse(page.get("enable_visualize").getAsBoolean(), page.get("name").getAsString());
        assertEquals("minecraft:purple_stained_glass", page.getAsJsonObject("multiblock")
            .getAsJsonObject("mapping").get(symbol).getAsString(), page.get("name").getAsString());
    }

    private void assertNotFusionRecipe(JsonObject page, String key) throws IOException {
        if (!page.has(key)) return;
        String recipe = page.get(key).getAsString();
        if (!recipe.startsWith("draconicevolution:")) return;
        String path = "data/draconicevolution/recipe/"
            + recipe.substring("draconicevolution:".length()) + ".json";
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(path)) {
            assertNotNull(input, recipe);
            JsonObject recipeJson = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8))
                .getAsJsonObject();
            assertFalse("draconicevolution:fusion_crafting".equals(recipeJson.get("type").getAsString()), recipe);
        }
    }

    private void assertNoViewerNames(JsonElement element, String path) {
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            String text = element.getAsString();
            assertFalse(text.matches("(?is).*\\b(?:JEI|Jade)\\b.*"), path + ": " + text);
        } else if (element.isJsonArray()) {
            element.getAsJsonArray().forEach(child -> assertNoViewerNames(child, path));
        } else if (element.isJsonObject()) {
            element.getAsJsonObject().entrySet().forEach(entry -> assertNoViewerNames(entry.getValue(), path));
        }
    }

    private void assertRecipeExists(JsonObject page, String key) {
        if (!page.has(key)) return;
        String recipe = page.get(key).getAsString();
        if (!recipe.startsWith("draconicevolution:")) return;
        String path = recipe.substring("draconicevolution:".length());
        assertNotNull(getClass().getClassLoader().getResource(
            "data/draconicevolution/recipe/" + path + ".json"), recipe);
    }

    private List<String> pageTypes(JsonObject entry) {
        return entry.getAsJsonArray("pages").asList().stream()
            .map(page -> page.getAsJsonObject().get("type").getAsString())
            .toList();
    }

    private JsonObject read(String path) throws IOException {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(path)) {
            assertNotNull(input, path);
            return JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }
}
