package dev.tassis.draconicinsight.smoke;

import com.google.gson.JsonObject;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@Mod(value = "tutorial_smoke", dist = Dist.CLIENT)
@EventBusSubscriber(modid = "tutorial_smoke", value = Dist.CLIENT)
public final class TutorialSmoke {
    private static boolean finished;
    private static JsonObject pending;

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (finished || mc.getOverlay() != null || (pending == null && mc.screen == null)) return;
        if (pending != null) {
            try {
                if (!TutorialVisualSmoke.tick(pending)) return;
            } catch (Throwable error) {
                pending.addProperty("passed", false);
                pending.addProperty("error", error.toString());
                error.printStackTrace();
            }
            finish(mc, pending);
            return;
        }
        JsonObject result = new JsonObject();
        result.addProperty("screen", mc.screen.getClass().getSimpleName());
        try {
            SmokeDistribution.verify(result);
            result.addProperty("neoforge", ModList.get().getModContainerById("neoforge").orElseThrow().getModInfo().getVersion().toString());
            result.addProperty("patchouli", ModList.get().isLoaded("patchouli"));
            result.addProperty("ponder", ModList.get().isLoaded("ponder"));
            result.addProperty("jei", ModList.get().getModContainerById("jei").orElseThrow().getModInfo().getVersion().toString());
            if (ModList.get().isLoaded("patchouli")) result.addProperty("reactorDiagrams", PatchouliSmoke.verify());
            if (ModList.get().isLoaded("ponder")) result.addProperty("ponderScenes", PonderSmoke.verify());
            result.addProperty("passed", true);
            if (Boolean.getBoolean("draconic_insight.smoke.visual")) {
                pending = result;
                return;
            }
        } catch (Throwable error) {
            result.addProperty("passed", false);
            result.addProperty("error", error.toString());
            error.printStackTrace();
        }
        finish(mc, result);
    }

    private static void finish(Minecraft mc, JsonObject result) {
        finished = true;
        try {
            Path path = Path.of(System.getProperty("draconic_insight.smoke.output"));
            Files.createDirectories(path.getParent());
            Files.writeString(path, result.toString());
            System.out.println("TUTORIAL_SMOKE_RESULT " + result);
        } catch (Exception error) {
            throw new IllegalStateException(error);
        }
        mc.stop();
    }
}
