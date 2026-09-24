package dev.tassis.draconicinsight.integration.client;

import dev.tassis.draconicinsight.DraconicInsight;
import dev.tassis.draconicinsight.format.EnergyText;
import dev.tassis.draconicinsight.integration.jei.CrystalInfoCatalog;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = DraconicInsight.MOD_ID, value = Dist.CLIENT)
public final class CrystalTooltipHandler {
    private CrystalTooltipHandler() {
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        String itemId = BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem()).toString();
        var entry = CrystalInfoCatalog.find(itemId);
        if (entry.isPresent()) {
            appendCrystalTooltip(event, entry.get());
        } else if (itemId.equals("draconicevolution:crystal_binder")) {
            event.getToolTip().add(Component.translatable("tooltip.draconic_insight.binder.connect")
                .withStyle(ChatFormatting.GRAY));
            event.getToolTip().add(Component.translatable("tooltip.draconic_insight.binder.clear")
                .withStyle(ChatFormatting.DARK_AQUA));
        }
    }

    private static void appendCrystalTooltip(ItemTooltipEvent event, CrystalInfoCatalog.Entry entry) {
        var tier = entry.tier();
        event.getToolTip().add(Component.translatable(
            "tooltip.draconic_insight.summary",
            Component.translatable("tier.draconic_insight." + tier.tierName()),
            EnergyText.op(tier.capacity())
        ).withStyle(ChatFormatting.AQUA));
        event.getToolTip().add(Component.translatable(
            "tooltip.draconic_insight.limits." + entry.kind().name().toLowerCase(java.util.Locale.ROOT),
            entry.maxLinks(),
            tier.range()
        ).withStyle(ChatFormatting.GRAY));
        if (Screen.hasShiftDown()) {
            event.getToolTip().add(Component.translatable(
                "tooltip.draconic_insight.kind." + entry.kind().name().toLowerCase(java.util.Locale.ROOT),
                EnergyText.opPerTick(entry.endpointTransferLimit()),
                entry.maxWirelessTargets()
            ).withStyle(ChatFormatting.GRAY));
        } else {
            event.getToolTip().add(Component.translatable("tooltip.draconic_insight.hold_shift")
                .withStyle(ChatFormatting.YELLOW));
        }
    }
}
