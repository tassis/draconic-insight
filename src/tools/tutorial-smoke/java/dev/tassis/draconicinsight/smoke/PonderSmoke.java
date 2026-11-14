package dev.tassis.draconicinsight.smoke;

import com.google.gson.JsonParser;
import dev.tassis.draconicinsight.DraconicInsight;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import net.createmod.ponder.foundation.PonderIndex;
import net.createmod.ponder.foundation.registration.PonderLocalization;
import net.createmod.ponder.foundation.registration.PonderSceneRegistry;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;

final class PonderSmoke {
    static int verify() throws Exception {
        com.google.gson.JsonObject english;
        try (var input = DraconicInsight.class.getResourceAsStream("/assets/draconic_insight/lang/en_us.json")) {
            english = JsonParser.parseString(new String(input.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
        }
        PonderIndex.gatherSharedText();
        var scenes = new HashSet<String>();
        var expected = new HashSet<String>();
        try (var input = DraconicInsight.class.getResourceAsStream("/assets/draconic_insight/ponder/tutorials.json")) {
            for (var element : JsonParser.parseString(new String(input.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonArray()) {
                expected.add(element.getAsJsonObject().get("id").getAsString());
            }
        }
        var localization = (PonderLocalization) PonderIndex.getLangAccess();
        for (var entry : PonderIndex.getSceneAccess().getRegisteredEntries()) {
            var storyboard = entry.getValue();
            if (!storyboard.getNamespace().equals("draconic_insight")) continue;
            String id = storyboard.getSchematicLocation().getPath();
            if (!scenes.add(id)) continue;
            PonderSceneRegistry.compileScene(localization, storyboard, null);
            String title = english.get("draconic_insight.ponder." + id + ".header").getAsString();
            if (!title.equals(localization.specific.get(storyboard.getSchematicLocation()).get("header"))) {
                throw new AssertionError("Scene title does not match en_us.json: " + id);
            }
            var template = PonderSceneRegistry.loadSchematic(storyboard.getSchematicLocation());
            if (template.getSize().getX() < 1) throw new AssertionError("Empty Ponder schematic: " + id);
            try (var input = DraconicInsight.class.getResourceAsStream("/assets/draconic_insight/ponder/" + id + ".nbt")) {
                var nbt = NbtIo.readCompressed(input, NbtAccounter.unlimitedHeap());
                for (var element : nbt.getList("palette", Tag.TAG_COMPOUND)) {
                    var state = (net.minecraft.nbt.CompoundTag) element;
                    String matcher = state.getString("Name");
                    if (state.contains("Properties")) {
                        var properties = state.getCompound("Properties");
                        matcher += "[" + String.join(",", properties.getAllKeys().stream()
                            .map(k -> k + "=" + properties.getString(k)).toList()) + "]";
                    }
                    BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK.asLookup(), matcher, false);
                }
            }
        }
        if (!scenes.equals(expected)) throw new AssertionError("Registered Ponder scenes do not match the catalog: " + scenes);
        String prefix = "draconic_insight.ponder.shared.";
        for (var entry : english.entrySet()) {
            if (!entry.getKey().startsWith(prefix)) continue;
            String key = entry.getKey().substring(prefix.length());
            String registered = localization.shared.get(
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("draconic_insight", key));
            if (!entry.getValue().getAsString().equals(registered)) {
                throw new AssertionError("Shared caption does not match en_us.json: " + key);
            }
        }
        return scenes.size();
    }
}
