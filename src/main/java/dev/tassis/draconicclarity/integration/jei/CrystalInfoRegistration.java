package dev.tassis.draconicclarity.integration.jei;

import dev.tassis.draconicclarity.DraconicClarity;
import dev.tassis.draconicclarity.format.EnergyText;
import java.util.List;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class CrystalInfoRegistration {
    private CrystalInfoRegistration() {
    }

    public static void register(IRecipeRegistration registration) {
        registerKind(registration, CrystalInfoCatalog.Kind.RELAY,
            "jei.draconic_clarity.crystal.relay");
        registerKind(registration, CrystalInfoCatalog.Kind.DIRECT_IO,
            "jei.draconic_clarity.crystal.direct_io");
        registerKind(registration, CrystalInfoCatalog.Kind.WIRELESS,
            "jei.draconic_clarity.crystal.wireless");

        resolveStack("draconicevolution:crystal_binder").ifPresent(stack ->
            registration.addItemStackInfo(stack,
                Component.translatable("jei.draconic_clarity.binder.select"),
                Component.translatable("jei.draconic_clarity.binder.connect"),
                Component.translatable("jei.draconic_clarity.binder.clear"),
                Component.translatable("jei.draconic_clarity.binder.limits")));
    }

    private static void registerKind(
        IRecipeRegistration registration,
        CrystalInfoCatalog.Kind kind,
        String descriptionKey
    ) {
        List<CrystalInfoCatalog.Entry> entries = CrystalInfoCatalog.entriesFor(kind);
        int registered = 0;
        for (CrystalInfoCatalog.Entry entry : entries) {
            var stack = resolveStack(entry.itemId());
            if (stack.isEmpty()) continue;
            registration.addItemStackInfo(stack.get(), informationLines(entry, descriptionKey));
            registered++;
        }
        if (registered == 0) {
            DraconicClarity.LOGGER.warn("No crystal items resolved for JEI information kind {}", kind);
        }
    }

    static Component[] informationLines(CrystalInfoCatalog.Entry entry, String descriptionKey) {
        var tier = entry.tier();
        return new Component[] {
            Component.translatable(descriptionKey),
            specificationLine(entry),
            Component.translatable("jei.draconic_clarity.crystal.setup." + kindKey(entry.kind())),
            Component.translatable("jei.draconic_clarity.crystal.live")
        };
    }

    private static Component specificationLine(CrystalInfoCatalog.Entry entry) {
        var tier = entry.tier();
        String key = "jei.draconic_clarity.crystal.limits." + kindKey(entry.kind());
        return switch (entry.kind()) {
            case RELAY -> Component.translatable(key,
                Component.translatable("tier.draconic_clarity." + tier.tierName()),
                EnergyText.op(tier.capacity()), entry.maxLinks(), tier.range());
            case DIRECT_IO -> Component.translatable(key,
                Component.translatable("tier.draconic_clarity." + tier.tierName()),
                EnergyText.op(tier.capacity()), entry.maxLinks(), tier.range(),
                EnergyText.opPerTick(entry.endpointTransferLimit()));
            case WIRELESS -> Component.translatable(key,
                Component.translatable("tier.draconic_clarity." + tier.tierName()),
                EnergyText.op(tier.capacity()), entry.maxLinks(), entry.maxWirelessTargets(), tier.range(),
                EnergyText.opPerTick(entry.endpointTransferLimit()));
        };
    }

    private static String kindKey(CrystalInfoCatalog.Kind kind) {
        return kind.name().toLowerCase(java.util.Locale.ROOT);
    }

    private static java.util.Optional<ItemStack> resolveStack(String id) {
        ResourceLocation location = ResourceLocation.parse(id);
        return BuiltInRegistries.ITEM.getOptional(location).map(ItemStack::new);
    }
}
