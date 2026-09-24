package dev.tassis.draconicclarity.integration.jade.client;

import dev.tassis.draconicclarity.integration.jade.FacilityJadeKeys;
import dev.tassis.draconicclarity.integration.jade.FacilityServerDataProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum FacilityComponentProvider implements IBlockComponentProvider {
    INSTANCE;
    @Override public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        var data = accessor.getServerData();
        if (!data.contains(FacilityJadeKeys.KIND)) return;
        String kind = data.getString(FacilityJadeKeys.KIND);
        tooltip.add(Component.translatable("jade.draconic_clarity.facility." + kind,
                data.getString(FacilityJadeKeys.TIER)));
        if (data.contains(FacilityJadeKeys.ENERGY)) tooltip.add(Component.translatable(
                "jade.draconic_clarity.facility.energy", data.getString(FacilityJadeKeys.ENERGY),
                data.getBoolean(FacilityJadeKeys.UNLIMITED) ? "∞" : Long.toString(data.getLong(FacilityJadeKeys.CAPACITY))));
        if (kind.equals("core")) tooltip.add(Component.translatable("jade.draconic_clarity.facility.core_state",
                data.getBoolean(FacilityJadeKeys.ACTIVE), data.getBoolean(FacilityJadeKeys.VALID), data.getBoolean(FacilityJadeKeys.STABILIZERS)));
        if (kind.equals("pylon")) tooltip.add(Component.translatable("jade.draconic_clarity.facility.pylon_state",
                data.getString(FacilityJadeKeys.MODE), data.getBoolean(FacilityJadeKeys.VALID), data.getBoolean(FacilityJadeKeys.LINKED)));
        if (kind.equals("fusion_core")) tooltip.add(Component.translatable("jade.draconic_clarity.facility.fusion_state",
                data.getString(FacilityJadeKeys.STATE), data.getBoolean(FacilityJadeKeys.CRAFTING), data.getInt(FacilityJadeKeys.INJECTORS), data.getInt(FacilityJadeKeys.OCCUPIED)));
        if (kind.equals("fusion_core") && data.contains(FacilityJadeKeys.PROGRESS)) tooltip.add(Component.translatable(
                "jade.draconic_clarity.facility.progress", formatPercent(data.getFloat(FacilityJadeKeys.PROGRESS))));
        if (kind.equals("injector")) {
            tooltip.add(Component.translatable("jade.draconic_clarity.facility.injector_state",
                    data.getBoolean(FacilityJadeKeys.LINKED), data.getString(FacilityJadeKeys.ITEM)));
            if (data.contains(FacilityJadeKeys.REQUIRED)) tooltip.add(Component.translatable(
                    "jade.draconic_clarity.facility.charge", data.getLong(FacilityJadeKeys.ENERGY),
                    data.getLong(FacilityJadeKeys.REQUIRED), data.getLong(FacilityJadeKeys.CHARGE_LIMIT)));
        }
    }
    private static String formatPercent(float progress) { return Math.round(progress * 1000F) / 10F + "%"; }
    @Override public ResourceLocation getUid() { return FacilityServerDataProvider.UID; }
}
