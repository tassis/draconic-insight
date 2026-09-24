package dev.tassis.draconicclarity;

import com.mojang.logging.LogUtils;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(DraconicClarity.MOD_ID)
public final class DraconicClarity {
    public static final String MOD_ID = "draconic_clarity";
    public static final Logger LOGGER = LogUtils.getLogger();

    public DraconicClarity() {
        LOGGER.info("Draconic Clarity initialized");
    }
}
