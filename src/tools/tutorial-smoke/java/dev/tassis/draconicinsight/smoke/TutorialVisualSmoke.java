package dev.tassis.draconicinsight.smoke;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.tassis.draconicinsight.DraconicInsight;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import net.createmod.ponder.foundation.PonderIndex;
import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.ui.PonderUI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import vazkii.patchouli.api.PatchouliAPI;

/** Optional real-world/render pass. Its scratch world lives only in run-tutorial-smoke. */
final class TutorialVisualSmoke {
    private static final List<Snapshot> snapshots = new ArrayList<>();
    private static boolean worldStarted;
    private static int warmup;
    private static int index;
    private static int frame;
    private static int ponderCount;

    static boolean tick(JsonObject result) throws Exception {
        Minecraft mc = Minecraft.getInstance();
        if (!worldStarted) {
            worldStarted = true;
            var settings = new LevelSettings("Tutorial verification", GameType.CREATIVE, false, Difficulty.PEACEFUL,
                true, new GameRules(), WorldDataConfiguration.DEFAULT);
            mc.createWorldOpenFlows().createFreshLevel("tutorial-visual-" + System.currentTimeMillis(), settings,
                new WorldOptions(0, false, false),
                registries -> registries.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT)
                    .value().createWorldDimensions(), mc.screen);
            return false;
        }
        if (mc.level == null || mc.player == null) return false;
        if (warmup++ < 40) return false;
        if (snapshots.isEmpty()) {
            result.addProperty("formedPlacementGuides", PatchouliSmoke.verifyFormedGuides());
            HashSet<String> ids = new HashSet<>();
            for (var entry : PonderIndex.getSceneAccess().getRegisteredEntries()) {
                var board = entry.getValue();
                if (!board.getNamespace().equals("draconic_insight") || !ids.add(board.getSchematicLocation().getPath())) continue;
                snapshots.add(new Snapshot("ponder-" + board.getSchematicLocation().getPath(), () -> {
                    var scene = PonderIndex.getSceneAccess().compile(List.of(board)).getFirst();
                    mc.setScreen(new SingleSceneScreen(scene));
                }));
            }
            ponderCount = ids.size();
            if (ponderCount != result.get("ponderScenes").getAsInt()) throw new AssertionError("Missing visual Ponder scenes");
            for (String entry : List.of("energy_storage/energy_core", "energy_storage/stabilizers",
                "energy_storage/energy_pylon", "fusion_crafting/setup", "reactor/setup")) {
                try (var input = DraconicInsight.class.getResourceAsStream(
                    "/assets/draconic_insight/patchouli_books/draconic_insight/en_us/entries/" + entry + ".json")) {
                    var pages = JsonParser.parseString(new String(input.readAllBytes(), StandardCharsets.UTF_8))
                        .getAsJsonObject().getAsJsonArray("pages");
                    HashSet<Integer> spreads = new HashSet<>();
                    book(entry, 0);
                    spreads.add(0);
                    for (int page = 0; page < pages.size(); page++) {
                        if (pages.get(page).getAsJsonObject().get("type").getAsString().equals("patchouli:multiblock")
                            && spreads.add(page / 2)) book(entry, page);
                    }
                }
            }
        }
        if (index == snapshots.size()) {
            result.addProperty("renderedPonderScenes", ponderCount);
            result.addProperty("screenshots", snapshots.size());
            return true;
        }
        Snapshot snapshot = snapshots.get(index);
        if (frame == 0) snapshot.open.run();
        if (frame == 10 && mc.screen instanceof PonderUI ui) {
            ui.seekToTime(ui.getActiveScene().getTotalTime() - 110);
        }
        if (frame == 30) {
            if (snapshot.name.startsWith("book-") && PatchouliAPI.get().getOpenBookGui() == null) {
                throw new AssertionError("Book screen did not open: " + snapshot.name);
            }
            Path folder = Path.of(System.getProperty("draconic_insight.smoke.output")).getParent().resolve("tutorial-screens");
            Files.createDirectories(folder);
            Screenshot.grab(folder.toFile(), snapshot.name + ".png", mc.getMainRenderTarget(), message -> {});
        }
        if (frame++ >= 45) {
            frame = 0;
            index++;
        }
        return false;
    }

    private static void book(String entry, int page) {
        snapshots.add(new Snapshot("book-" + entry.replace('/', '-') + "-" + page, () ->
            PatchouliAPI.get().openBookEntry(ResourceLocation.parse("draconic_insight:draconic_insight"),
                ResourceLocation.fromNamespaceAndPath("draconic_insight", entry), page)));
    }

    private record Snapshot(String name, Runnable open) {
    }

    private static final class SingleSceneScreen extends PonderUI {
        private SingleSceneScreen(PonderScene scene) {
            super(List.of(scene));
        }
    }
}
