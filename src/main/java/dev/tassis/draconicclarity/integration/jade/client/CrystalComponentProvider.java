package dev.tassis.draconicclarity.integration.jade.client;

import dev.tassis.draconicclarity.format.EnergyText;
import dev.tassis.draconicclarity.integration.jade.CrystalJadeKeys;
import dev.tassis.draconicclarity.integration.jade.CrystalServerDataProvider;
import java.math.BigDecimal;
import java.math.RoundingMode;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum CrystalComponentProvider implements IBlockComponentProvider {
    INSTANCE;

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
            "jade.draconic_clarity.charge",
            formatPercent(data.getDouble(CrystalJadeKeys.CHARGE))
        ));
        tooltip.add(Component.translatable(
            "jade.draconic_clarity.links",
            data.getInt(CrystalJadeKeys.LINKS),
            data.getInt(CrystalJadeKeys.MAX_LINKS)
        ));
        tooltip.add(Component.translatable(
            "jade.draconic_clarity.range",
            data.getInt(CrystalJadeKeys.RANGE)
        ));
        tooltip.add(Component.translatable(
            "jade.draconic_clarity.flow",
            EnergyText.compact(data.getLong(CrystalJadeKeys.LINK_FLOW))
        ));
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
            tooltip.add(Component.translatable(
                "jade.draconic_clarity.wireless_flow." + mode,
                EnergyText.compact(data.getLong(CrystalJadeKeys.WIRELESS_FLOW))
            ));
        }
    }

    private static String formatPercent(double value) {
        return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }

    @Override
    public ResourceLocation getUid() {
        return CrystalServerDataProvider.UID;
    }
}
