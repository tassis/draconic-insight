package dev.tassis.draconicclarity.integration.jade;

import com.brandon3055.draconicevolution.blocks.tileentity.TileEnergyCore;
import com.brandon3055.draconicevolution.blocks.tileentity.TileEnergyPylon;
import com.brandon3055.draconicevolution.blocks.tileentity.TileFusionCraftingCore;
import com.brandon3055.draconicevolution.blocks.tileentity.TileFusionCraftingInjector;
import dev.tassis.draconicclarity.DraconicClarity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

public enum FacilityServerDataProvider implements IServerDataProvider<BlockAccessor> {
    INSTANCE;
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(DraconicClarity.MOD_ID, "facility_state");

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof TileEnergyCore core) writeCore(data, core);
        else if (accessor.getBlockEntity() instanceof TileEnergyPylon pylon) writePylon(data, pylon);
        else if (accessor.getBlockEntity() instanceof TileFusionCraftingCore core) writeFusionCore(data, core);
        else if (accessor.getBlockEntity() instanceof TileFusionCraftingInjector injector) writeInjector(data, injector);
    }

    private static void writeCore(CompoundTag data, TileEnergyCore core) {
        data.putString(FacilityJadeKeys.KIND, "core");
        data.putInt(FacilityJadeKeys.TIER, core.tier.get());
        data.putBoolean(FacilityJadeKeys.ACTIVE, core.active.get());
        data.putBoolean(FacilityJadeKeys.VALID, core.coreValid.get());
        data.putBoolean(FacilityJadeKeys.STABILIZERS, core.stabilizersValid.get());
        data.putString(FacilityJadeKeys.ENERGY, core.energy.getStoredBig().toString());
        if (core.energy.isUnlimited()) data.putBoolean(FacilityJadeKeys.UNLIMITED, true);
        else data.putLong(FacilityJadeKeys.CAPACITY, core.energy.getMaxOPStored());
    }

    private static void writePylon(CompoundTag data, TileEnergyPylon pylon) {
        data.putString(FacilityJadeKeys.KIND, "pylon");
        data.putString(FacilityJadeKeys.MODE, pylon.ioMode.get().name().toLowerCase(java.util.Locale.ROOT));
        data.putBoolean(FacilityJadeKeys.VALID, pylon.structureValid.get());
        var offset = pylon.coreOffset.get();
        if (offset == null) return;
        var level = pylon.getLevel();
        var corePos = pylon.getBlockPos().subtract(offset);
        if (level == null || !level.hasChunkAt(corePos) || !(level.getBlockEntity(corePos) instanceof TileEnergyCore core)) return;
        data.putBoolean(FacilityJadeKeys.LINKED, true);
        data.putInt(FacilityJadeKeys.TIER, core.tier.get());
        data.putBoolean(FacilityJadeKeys.ACTIVE, core.active.get());
        data.putString(FacilityJadeKeys.ENERGY, core.energy.getStoredBig().toString());
        if (core.energy.isUnlimited()) data.putBoolean(FacilityJadeKeys.UNLIMITED, true);
        else data.putLong(FacilityJadeKeys.CAPACITY, core.energy.getMaxOPStored());
    }

    private static void writeFusionCore(CompoundTag data, TileFusionCraftingCore core) {
        data.putString(FacilityJadeKeys.KIND, "fusion_core");
        data.putBoolean(FacilityJadeKeys.CRAFTING, core.isCrafting());
        data.putString(FacilityJadeKeys.STATE, core.getFusionState().name().toLowerCase(java.util.Locale.ROOT));
        float progress = core.progress.get();
        if (progress >= 0F && progress <= 1F) data.putFloat(FacilityJadeKeys.PROGRESS, progress);
        var injectors = core.getInjectors();
        data.putInt(FacilityJadeKeys.INJECTORS, injectors.size());
        int occupied = (int) injectors.stream().filter(injector -> !injector.getInjectorStack().isEmpty()).count();
        data.putInt(FacilityJadeKeys.OCCUPIED, occupied);
        if (occupied > 0) data.putString(FacilityJadeKeys.TIER, core.getMinimumTier().name());
    }

    private static void writeInjector(CompoundTag data, TileFusionCraftingInjector injector) {
        data.putString(FacilityJadeKeys.KIND, "injector");
        data.putString(FacilityJadeKeys.TIER, injector.getInjectorTier().name());
        var corePosition = injector.corePos.get().getPos();
        var level = injector.getLevel();
        data.putBoolean(FacilityJadeKeys.LINKED, level != null && level.hasChunkAt(corePosition)
                && level.getBlockEntity(corePosition) instanceof TileFusionCraftingCore);
        var stack = injector.getInjectorStack();
        if (!stack.isEmpty()) data.putString(FacilityJadeKeys.ITEM, stack.getHoverName().getString());
        long required = injector.getEnergyRequirement();
        if (required > 0L) {
            data.putLong(FacilityJadeKeys.ENERGY, injector.getInjectorEnergy());
            data.putLong(FacilityJadeKeys.REQUIRED, required);
            data.putLong(FacilityJadeKeys.CHARGE_LIMIT, injector.chargeRate.get());
        }
    }

    @Override public ResourceLocation getUid() { return UID; }
}
