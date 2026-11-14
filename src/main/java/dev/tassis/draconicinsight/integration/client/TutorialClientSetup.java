package dev.tassis.draconicinsight.integration.client;

import dev.tassis.draconicinsight.DraconicInsight;
import dev.tassis.draconicinsight.integration.patchouli.client.HandbookMultiblocks;
import dev.tassis.draconicinsight.integration.ponder.DraconicPonderPlugin;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(value = DraconicInsight.MOD_ID, dist = Dist.CLIENT)
public final class TutorialClientSetup {
    public TutorialClientSetup(IEventBus modEventBus) {
        modEventBus.addListener(TutorialClientSetup::setup);
    }

    public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            if (ModList.get().isLoaded("patchouli")) {
                HandbookMultiblocks.register();
            }
            if (ModList.get().isLoaded("ponder")) {
                DraconicPonderPlugin.register();
            }
        });
    }
}
