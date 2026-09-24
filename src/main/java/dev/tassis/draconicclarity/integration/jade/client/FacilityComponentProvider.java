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
        if (data.contains(FacilityJadeKeys.ENERGY)) tooltip.add(Component.translatable(
                "jade.draconic_clarity.facility.energy", data.getString(FacilityJadeKeys.ENERGY),
                data.getBoolean(FacilityJadeKeys.UNLIMITED) ? "∞" : Long.toString(data.getLong(FacilityJadeKeys.CAPACITY))));
        if (kind.equals("core")) appendCoreWarnings(tooltip, data);
        if (kind.equals("pylon")) appendPylonState(tooltip, data);
        if (kind.equals("fusion_core")) appendFusionState(tooltip, data);
        if (kind.equals("injector")) {
            if (!data.getBoolean(FacilityJadeKeys.LINKED)) tooltip.add(Component.translatable("jade.draconic_clarity.facility.warning.unlinked"));
            if (data.contains(FacilityJadeKeys.ITEM)) tooltip.add(Component.translatable(
                    "jade.draconic_clarity.facility.item", data.getString(FacilityJadeKeys.ITEM)));
            if (data.contains(FacilityJadeKeys.REQUIRED)) tooltip.add(Component.translatable(
                    "jade.draconic_clarity.facility.charge", data.getLong(FacilityJadeKeys.ENERGY),
                    data.getLong(FacilityJadeKeys.REQUIRED), data.getLong(FacilityJadeKeys.CHARGE_LIMIT)));
        }
    }

    private static void appendCoreWarnings(ITooltip tooltip, net.minecraft.nbt.CompoundTag data) {
        if (!data.getBoolean(FacilityJadeKeys.VALID)) tooltip.add(Component.translatable("jade.draconic_clarity.facility.warning.core_incomplete"));
        if (!data.getBoolean(FacilityJadeKeys.STABILIZERS)) tooltip.add(Component.translatable("jade.draconic_clarity.facility.warning.stabilizers_incomplete"));
    }

    private static void appendPylonState(ITooltip tooltip, net.minecraft.nbt.CompoundTag data) {
        tooltip.add(Component.translatable("jade.draconic_clarity.mode." + data.getString(FacilityJadeKeys.MODE)));
        if (!data.getBoolean(FacilityJadeKeys.VALID)) tooltip.add(Component.translatable("jade.draconic_clarity.facility.warning.pylon_incomplete"));
        if (!data.getBoolean(FacilityJadeKeys.LINKED)) tooltip.add(Component.translatable("jade.draconic_clarity.facility.warning.unlinked"));
    }

    private static void appendFusionState(ITooltip tooltip, net.minecraft.nbt.CompoundTag data) {
        int injectors = data.getInt(FacilityJadeKeys.INJECTORS);
        int occupied = data.getInt(FacilityJadeKeys.OCCUPIED);
        if (occupied > 0) tooltip.add(Component.translatable(
                "jade.draconic_clarity.facility.injectors_occupied", injectors, occupied));
        else tooltip.add(Component.translatable("jade.draconic_clarity.facility.injectors", injectors));
        if (data.getBoolean(FacilityJadeKeys.CRAFTING) && data.contains(FacilityJadeKeys.PROGRESS)) tooltip.add(Component.translatable(
                "jade.draconic_clarity.facility.progress", formatPercent(data.getFloat(FacilityJadeKeys.PROGRESS))));
    }
    private static String formatPercent(float progress) { return Math.round(progress * 1000F) / 10F + "%"; }
    @Override public ResourceLocation getUid() { return FacilityServerDataProvider.UID; }
}
