package dev.tassis.draconicinsight;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import org.junit.jupiter.api.Test;

class MultiblockTutorialTest {
    private static final String ASSETS = "assets/draconic_insight/";
    private static final String BOOK = ASSETS + "patchouli_books/draconic_insight/";
    private static final List<String> ENTRIES = List.of("energy_storage/energy_core", "energy_storage/stabilizers",
        "energy_storage/energy_pylon", "fusion_crafting/setup", "reactor/setup");

    @Test
    void everyLocaleUsesTheSameGeometryAndAnchorsOnTheIntendedBlock() throws IOException {
        for (String entry : ENTRIES) {
            JsonArray english = json(BOOK + "en_us/entries/" + entry + ".json").getAsJsonArray("pages");
            for (String locale : List.of("en_us", "zh_tw", "zh_cn")) {
                JsonArray pages = json(BOOK + locale + "/entries/" + entry + ".json").getAsJsonArray("pages");
                assertEquals(english.size(), pages.size());
                for (int i = 0; i < pages.size(); i++) {
                    JsonObject page = pages.get(i).getAsJsonObject();
                    if (!page.has("multiblock")) continue;
                    JsonObject definition = page.getAsJsonObject("multiblock");
                    assertEquals(english.get(i).getAsJsonObject().get("multiblock"), definition);
                    assertEquals(english.get(i).getAsJsonObject().get("multiblock_id"), page.get("multiblock_id"));
                    Map<Pos, Character> cells = cells(definition.getAsJsonArray("pattern"));
                    Pos zero = find(cells, '0');
                    Pos anchor = zero.add(pos(definition.getAsJsonArray("offset")));
                    char symbol = cells.getOrDefault(anchor, ' ');
                    String expected = entry.equals("energy_storage/energy_pylon") ? "P"
                        : entry.equals("energy_storage/stabilizers") && !definition.getAsJsonObject("mapping").has("X") ? "S"
                        : "XCDR";
                    assertTrue(expected.indexOf(symbol) >= 0, locale + ": " + page.get("name") + " anchor=" + anchor);
                }
            }
        }
    }

    @Test
    void injectorDirectionsAndClearChannelsMatchActualPatchouliAxes() throws IOException {
        JsonObject definition = json(BOOK + "en_us/entries/fusion_crafting/setup.json")
            .getAsJsonArray("pages").asList().stream().map(e -> e.getAsJsonObject())
            .filter(p -> p.has("multiblock")).findFirst().orElseThrow().getAsJsonObject("multiblock");
        Map<Pos, Character> cells = cells(definition.getAsJsonArray("pattern"));
        Pos core = find(cells, 'C');
        JsonObject mapping = definition.getAsJsonObject("mapping");
        int injectors = 0;
        for (var cell : cells.entrySet()) {
            if ("EWSN".indexOf(cell.getValue()) < 0) continue;
            injectors++;
            Pos p = cell.getKey();
            Pos delta = core.subtract(p);
            String facing = Math.abs(delta.x) > Math.abs(delta.z)
                ? delta.x > 0 ? "east" : "west" : delta.z > 0 ? "south" : "north";
            assertEquals("draconicevolution:basic_crafting_injector[facing=" + facing + "]",
                mapping.get(cell.getValue().toString()).getAsString());
            assertEquals(3, Math.max(Math.abs(delta.x), Math.abs(delta.z)));
            assertEquals(1, Math.min(Math.abs(delta.x), Math.abs(delta.z)));
            Pos step = new Pos(Integer.signum(delta.x) * (Math.abs(delta.x) == 3 ? 1 : 0), 0,
                Integer.signum(delta.z) * (Math.abs(delta.z) == 3 ? 1 : 0));
            for (int d = 1; d < 3; d++) assertEquals(' ', cells.get(p.add(step.scale(d))));
        }
        assertEquals(8, injectors);
    }

    @Test
    void reactorInjectorIsAboveCoreAndUsesTheOrientedPreview() throws IOException {
        JsonObject page = json(BOOK + "en_us/entries/reactor/setup.json")
            .getAsJsonArray("pages").asList().stream().map(e -> e.getAsJsonObject())
            .filter(p -> p.has("multiblock_id") && p.get("multiblock_id").getAsString().endsWith(":reactor_injector"))
            .findFirst().orElseThrow();
        Map<Pos, Character> cells = cells(page.getAsJsonObject("multiblock").getAsJsonArray("pattern"));
        assertEquals(new Pos(0, 4, 0), find(cells, 'I').subtract(find(cells, 'C')));
        assertEquals("draconic_insight:reactor_injector", page.get("multiblock_id").getAsString());
    }

    @Test
    void allEightPonderCoreShellsMatchThePinnedUpstreamDefinitions() throws IOException {
        JsonArray tutorials = array(ASSETS + "ponder/tutorials.json");
        for (int tier = 1; tier <= 8; tier++) {
            JsonObject tutorial = tutorials.get(tier - 1).getAsJsonObject();
            Pos core = pos(tutorial.getAsJsonArray("core"));
            Map<Pos, String> schematic = structure(tutorial.get("id").getAsString());
            JsonObject upstream = json("data/draconicevolution/multiblocks/energy_core_" + tier + ".json");
            Pos origin = upstream.has("origin") ? new Pos(upstream.getAsJsonObject("origin").get("x").getAsInt(),
                upstream.getAsJsonObject("origin").get("y").getAsInt(), upstream.getAsJsonObject("origin").get("z").getAsInt()) : new Pos(0, 0, 0);
            Map<Pos, String> expected = new HashMap<>();
            Map<Character, String> material = Map.of('D', "draconicevolution:draconium_block", 'R', "minecraft:redstone_block",
                'A', "draconicevolution:awakened_draconium_block", 'X', "draconicevolution:energy_core");
            cells(upstream.getAsJsonArray("structure")).forEach((p, symbol) -> {
                if (material.containsKey(symbol)) expected.put(p.subtract(origin), material.get(symbol));
            });
            Map<Pos, String> actual = new HashMap<>();
            schematic.forEach((p, block) -> {
                if (p.y > 0 && !block.equals("draconicevolution:energy_core_stabilizer")) actual.put(p.subtract(core), block);
            });
            assertEquals(expected, actual, "Tier " + tier);
            long stabilizers = schematic.values().stream().filter("draconicevolution:energy_core_stabilizer"::equals).count();
            assertEquals(tier >= 5 ? 36 : 4, stabilizers);
        }
    }

    @Test
    void everyPonderStageHasRealBlocksAndAllThreeTranslations() throws IOException {
        JsonArray tutorials = array(ASSETS + "ponder/tutorials.json");
        assertEquals(19, tutorials.size());
        Set<String> ids = new HashSet<>();
        for (var element : tutorials) {
            JsonObject tutorial = element.getAsJsonObject();
            assertFalse(tutorial.has("title"), "Scene titles belong in language files");
            String id = tutorial.get("id").getAsString();
            assertTrue(ids.add(id), id);
            Map<Pos, String> schematic = structure(id);
            Set<Pos> covered = new HashSet<>();
            if (tutorial.has("initial")) {
                for (var xyz : tutorial.getAsJsonArray("initial")) {
                    Pos p = pos(xyz.getAsJsonArray());
                    assertTrue(schematic.containsKey(p), id + ": " + p);
                    covered.add(p);
                }
            }
            for (var stageElement : tutorial.getAsJsonArray("stages")) {
                JsonObject stage = stageElement.getAsJsonObject();
                assertFalse(stage.getAsJsonArray("positions").isEmpty());
                for (var xyz : stage.getAsJsonArray("positions")) {
                    Pos p = pos(xyz.getAsJsonArray());
                    assertTrue(schematic.containsKey(p), id + ": " + p);
                    covered.add(p);
                }
                for (String locale : List.of("en_us", "zh_tw", "zh_cn")) {
                    JsonObject lang = json(ASSETS + "lang/" + locale + ".json");
                    assertTrue(lang.has("draconic_insight.ponder.shared." + stage.get("caption").getAsString()));
                    assertTrue(lang.has("draconic_insight.ponder." + id + ".header"));
                }
            }
            assertEquals(schematic.keySet().stream().filter(p -> p.y > 0).collect(java.util.stream.Collectors.toSet()), covered, id);
        }
    }

    private Map<Pos, String> structure(String id) throws IOException {
        try (var input = getClass().getClassLoader().getResourceAsStream(ASSETS + "ponder/" + id + ".nbt")) {
            assertNotNull(input, id);
            CompoundTag nbt = NbtIo.readCompressed(input, NbtAccounter.unlimitedHeap());
            var palette = nbt.getList("palette", Tag.TAG_COMPOUND);
            var blocks = nbt.getList("blocks", Tag.TAG_COMPOUND);
            Map<Pos, String> result = new HashMap<>();
            for (var element : blocks) {
                CompoundTag block = (CompoundTag) element;
                var xyz = block.getList("pos", Tag.TAG_INT);
                result.put(new Pos(xyz.getInt(0), xyz.getInt(1), xyz.getInt(2)), palette.getCompound(block.getInt("state")).getString("Name"));
            }
            return result;
        }
    }

    private Map<Pos, Character> cells(JsonArray pattern) {
        Map<Pos, Character> result = new HashMap<>();
        for (int layer = 0; layer < pattern.size(); layer++) {
            JsonArray rows = pattern.get(layer).getAsJsonArray();
            for (int x = 0; x < rows.size(); x++) {
                String row = rows.get(x).getAsString();
                for (int z = 0; z < row.length(); z++) result.put(new Pos(x, pattern.size() - layer - 1, z), row.charAt(z));
            }
        }
        return result;
    }

    private Pos find(Map<Pos, Character> cells, char symbol) {
        return cells.entrySet().stream().filter(e -> e.getValue() == symbol).map(Map.Entry::getKey).findFirst().orElseThrow();
    }

    private Pos pos(JsonArray xyz) {
        return new Pos(xyz.get(0).getAsInt(), xyz.get(1).getAsInt(), xyz.get(2).getAsInt());
    }

    private JsonObject json(String path) throws IOException {
        return resource(path).getAsJsonObject();
    }

    private JsonArray array(String path) throws IOException {
        return resource(path).getAsJsonArray();
    }

    private com.google.gson.JsonElement resource(String path) throws IOException {
        try (var input = getClass().getClassLoader().getResourceAsStream(path)) {
            assertNotNull(input, path);
            return JsonParser.parseString(new String(input.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    private record Pos(int x, int y, int z) {
        Pos add(Pos other) { return new Pos(x + other.x, y + other.y, z + other.z); }
        Pos subtract(Pos other) { return new Pos(x - other.x, y - other.y, z - other.z); }
        Pos scale(int by) { return new Pos(x * by, y * by, z * by); }
    }
}
