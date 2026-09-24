package dev.tassis.draconicinsight;

import com.mojang.logging.LogUtils;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(DraconicInsight.MOD_ID)
public final class DraconicInsight {
    public static final String MOD_ID = "draconic_insight";
    public static final Logger LOGGER = LogUtils.getLogger();

    public DraconicInsight() {
        LOGGER.info("Draconic Insight initialized");
    }
}
