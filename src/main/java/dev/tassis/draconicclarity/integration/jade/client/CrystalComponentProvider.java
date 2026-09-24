package dev.tassis.draconicclarity.integration.jade.client;

import dev.tassis.draconicclarity.format.EnergyText;
import dev.tassis.draconicclarity.DraconicClarity;
import dev.tassis.draconicclarity.integration.jade.CrystalJadeKeys;
import dev.tassis.draconicclarity.integration.jade.CrystalServerDataProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum CrystalComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    public static final ResourceLocation FLOW_CONFIG = ResourceLocation.fromNamespaceAndPath(
        DraconicClarity.MOD_ID,
        "crystal_flow"
    );

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        var data = accessor.getServerData();
        if (!data.contains(CrystalJadeKeys.CAPACITY)) return;

        tooltip.add(Component.translatable(
            "jade.draconic_clarity.energy",
            EnergyText.compact(data.getLong(CrystalJadeKeys.ENERGY)),
            EnergyText.compact(data.getLong(CrystalJadeKeys.CAPACITY))
        ));
        tooltip.add(Component.translatable(
            "jade.draconic_clarity.links",
            data.getInt(CrystalJadeKeys.LINKS),
            data.getInt(CrystalJadeKeys.MAX_LINKS)
        ));
        if (config.get(FLOW_CONFIG) && data.getInt(CrystalJadeKeys.LINKS) > 0) {
            tooltip.add(Component.translatable(
                "jade.draconic_clarity.flow",
                EnergyText.compact(data.getLong(CrystalJadeKeys.LINK_FLOW))
            ));
        }
        if (data.contains(CrystalJadeKeys.MODE)) {
            String mode = data.getString(CrystalJadeKeys.MODE);
            tooltip.add(Component.translatable("jade.draconic_clarity.mode." + mode));
        }
        if (data.contains(CrystalJadeKeys.MAX_RECEIVERS)) {
            tooltip.add(Component.translatable(
                "jade.draconic_clarity.receivers",
                data.getInt(CrystalJadeKeys.RECEIVERS),
                data.getInt(CrystalJadeKeys.MAX_RECEIVERS)
            ));
            String mode = data.getString(CrystalJadeKeys.MODE);
            if (config.get(FLOW_CONFIG)) {
                tooltip.add(Component.translatable(
                    "jade.draconic_clarity.wireless_flow." + mode,
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
