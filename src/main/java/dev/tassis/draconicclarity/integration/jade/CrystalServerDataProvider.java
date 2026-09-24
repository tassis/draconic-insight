package dev.tassis.draconicclarity.integration.jade;

import com.brandon3055.draconicevolution.blocks.energynet.tileentity.TileCrystalBase;
import com.brandon3055.draconicevolution.blocks.energynet.tileentity.TileCrystalDirectIO;
import com.brandon3055.draconicevolution.blocks.energynet.tileentity.TileCrystalWirelessIO;
import dev.tassis.draconicclarity.DraconicClarity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

public enum CrystalServerDataProvider implements IServerDataProvider<BlockAccessor> {
    INSTANCE;

    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(
        DraconicClarity.MOD_ID,
        "crystal_state"
    );

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (!(accessor.getBlockEntity() instanceof TileCrystalBase crystal)) return;

        String mode = "";
        int receivers = 0;
        int maxReceivers = 0;
        long wirelessFlow = 0;
        if (crystal instanceof TileCrystalDirectIO directIO) {
            mode = directIO.outputMode.get() ? "output" : "input";
        } else if (crystal instanceof TileCrystalWirelessIO wireless) {
            mode = wireless.inputMode.get() ? "input" : "output";
            receivers = wireless.getReceivers().size();
            maxReceivers = wireless.getMaxReceivers();
            int samples = Math.min(receivers, wireless.receiverTransferRates.size());
            for (int index = 0; index < samples; index++) {
                wirelessFlow += Math.max(0, wireless.receiverTransfer(index));
            }
        }

        long linkFlow = 0;
        for (int index = 0; index < crystal.getLinks().size(); index++) {
            linkFlow += Math.max(0, crystal.getLinkFlow(index));
        }

        CrystalJadeData.write(data, new CrystalSnapshot(
            crystal.getEnergyStored(),
            crystal.getMaxEnergyStored(),
            crystal.getLinks().size(),
            crystal.maxLinks(),
            linkFlow,
            mode,
            receivers,
            maxReceivers,
            wirelessFlow
        ));
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
