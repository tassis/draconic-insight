package dev.tassis.draconicinsight.integration.jade.client;

import dev.tassis.draconicinsight.format.EnergyText;
import dev.tassis.draconicinsight.DraconicInsight;
import dev.tassis.draconicinsight.integration.jade.CrystalJadeKeys;
import dev.tassis.draconicinsight.integration.jade.CrystalServerDataProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum CrystalComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    public static final ResourceLocation FLOW_CONFIG = ResourceLocation.fromNamespaceAndPath(
        DraconicInsight.MOD_ID,
        "crystal_flow"
    );

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        var data = accessor.getServerData();
        if (!data.contains(CrystalJadeKeys.CAPACITY)) return;

        tooltip.add(Component.translatable(
            "jade.draconic_insight.energy",
            EnergyText.compact(data.getLong(CrystalJadeKeys.ENERGY)),
            EnergyText.compact(data.getLong(CrystalJadeKeys.CAPACITY))
        ));
        tooltip.add(Component.translatable(
            "jade.draconic_insight.links",
            data.getInt(CrystalJadeKeys.LINKS),
            data.getInt(CrystalJadeKeys.MAX_LINKS)
        ));
        if (config.get(FLOW_CONFIG) && data.getInt(CrystalJadeKeys.LINKS) > 0) {
            tooltip.add(Component.translatable(
                "jade.draconic_insight.flow",
                EnergyText.compact(data.getLong(CrystalJadeKeys.LINK_FLOW))
            ));
        }
        if (data.contains(CrystalJadeKeys.MODE)) {
            String mode = data.getString(CrystalJadeKeys.MODE);
            tooltip.add(Component.translatable("jade.draconic_insight.mode." + mode));
        }
        if (data.contains(CrystalJadeKeys.MAX_RECEIVERS)) {
            tooltip.add(Component.translatable(
                "jade.draconic_insight.receivers",
                data.getInt(CrystalJadeKeys.RECEIVERS),
                data.getInt(CrystalJadeKeys.MAX_RECEIVERS)
            ));
            String mode = data.getString(CrystalJadeKeys.MODE);
            if (config.get(FLOW_CONFIG)) {
                tooltip.add(Component.translatable(
                    "jade.draconic_insight.wireless_flow." + mode,
                    EnergyText.compact(data.getLong(CrystalJadeKeys.WIRELESS_FLOW))
                ));
            }
        }
    }

    @Override
    public ResourceLocation getUid() {
        return CrystalServerDataProvider.UID;
    }

    @Override
    public boolean enabledByDefault() {
        // Draconic Evolution already exposes crystal charge, links, and modes through Brandon's Core HUD.
        return false;
    }
}
