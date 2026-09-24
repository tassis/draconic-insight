package dev.tassis.draconicclarity.integration.client;

import dev.tassis.draconicclarity.DraconicClarity;
import dev.tassis.draconicclarity.format.EnergyText;
import dev.tassis.draconicclarity.integration.jei.CrystalInfoCatalog;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = DraconicClarity.MOD_ID, value = Dist.CLIENT)
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
            event.getToolTip().add(Component.translatable("tooltip.draconic_clarity.binder.connect")
                .withStyle(ChatFormatting.GRAY));
            event.getToolTip().add(Component.translatable("tooltip.draconic_clarity.binder.clear")
                .withStyle(ChatFormatting.DARK_AQUA));
        }
    }

    private static void appendCrystalTooltip(ItemTooltipEvent event, CrystalInfoCatalog.Entry entry) {
        var tier = entry.tier();
        event.getToolTip().add(Component.translatable(
            "tooltip.draconic_clarity.summary",
            Component.translatable("tier.draconic_clarity." + tier.tierName()),
            EnergyText.op(tier.capacity())
        ).withStyle(ChatFormatting.AQUA));
        event.getToolTip().add(Component.translatable(
            "tooltip.draconic_clarity.limits." + entry.kind().name().toLowerCase(java.util.Locale.ROOT),
            entry.maxLinks(),
            tier.range()
        ).withStyle(ChatFormatting.GRAY));
        if (Screen.hasShiftDown()) {
            event.getToolTip().add(Component.translatable(
                "tooltip.draconic_clarity.kind." + entry.kind().name().toLowerCase(java.util.Locale.ROOT),
                EnergyText.opPerTick(entry.endpointTransferLimit()),
                entry.maxWirelessTargets()
            ).withStyle(ChatFormatting.GRAY));
        } else {
            event.getToolTip().add(Component.translatable("tooltip.draconic_clarity.hold_shift")
                .withStyle(ChatFormatting.YELLOW));
        }
    }
}
