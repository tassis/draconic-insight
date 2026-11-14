package dev.tassis.draconicinsight.smoke;

import com.google.gson.JsonObject;
import java.nio.file.Files;
import java.nio.file.Path;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@Mod("tutorial_smoke")
public final class TutorialServerSmoke {
    public TutorialServerSmoke() {
        if (Boolean.getBoolean("draconic_insight.smoke.server")) {
            NeoForge.EVENT_BUS.addListener(TutorialServerSmoke::started);
        }
    }

    private static void started(ServerStartedEvent event) {
        JsonObject result = new JsonObject();
        try {
            SmokeDistribution.verify(result);
            result.addProperty("passed", true);
        } catch (Throwable error) {
            result.addProperty("passed", false);
            result.addProperty("error", error.toString());
            error.printStackTrace();
        }
        try {
            Path output = Path.of(System.getProperty("draconic_insight.smoke.output"));
            Files.createDirectories(output.getParent());
            Files.writeString(output, result.toString());
            System.out.println("TUTORIAL_SERVER_SMOKE_RESULT " + result);
        } catch (Exception error) {
            throw new IllegalStateException(error);
        } finally {
            event.getServer().halt(false);
        }
    }
}
