package dev.tassis.draconicinsight.smoke;

import com.google.gson.JsonObject;
import net.neoforged.fml.ModList;

final class SmokeDistribution {
    static void verify(JsonObject result) {
        var mods = ModList.get();
        boolean packaged = Boolean.getBoolean("draconic_insight.smoke.packaged");
        result.addProperty("packaged", packaged);
        String source = source("draconic_insight");
        result.addProperty("draconicSource", source);
        if (packaged && !source.endsWith(".jar")) throw new AssertionError("Production mod is not loaded from a JAR: " + source);
        for (String id : new String[] {"ponder", "flywheel", "patchouli", "create"}) {
            String setting = id.equals("flywheel") ? "Ponder" : Character.toUpperCase(id.charAt(0)) + id.substring(1);
            boolean expected = Boolean.getBoolean("draconic_insight.smoke.expected" + setting);
            if (mods.isLoaded(id) != expected) throw new AssertionError("Unexpected mod presence: " + id);
            if (!expected) continue;
            long count = mods.getMods().stream().filter(mod -> mod.getModId().equals(id)).count();
            if (count != 1) throw new AssertionError("Expected exactly one " + id + ", got " + count);
            result.addProperty(id + "Version", mods.getModContainerById(id).orElseThrow().getModInfo().getVersion().toString());
            String librarySource = source(id);
            result.addProperty(id + "Source", librarySource);
            if (packaged && (id.equals("ponder") || id.equals("flywheel")) && !librarySource.contains("jarjar")) {
                throw new AssertionError("Library is not loaded from Jar-in-Jar: " + librarySource);
            }
        }
    }

    private static String source(String id) {
        return ModList.get().getModFileById(id).getFile().getFilePath().toUri().toString();
    }
}
