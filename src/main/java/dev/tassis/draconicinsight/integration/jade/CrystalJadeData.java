package dev.tassis.draconicinsight.integration.jade;

import net.minecraft.nbt.CompoundTag;

public final class CrystalJadeData {
    private CrystalJadeData() {
    }

    public static void write(CompoundTag data, CrystalSnapshot snapshot) {
        data.putLong(CrystalJadeKeys.ENERGY, snapshot.energy());
        data.putLong(CrystalJadeKeys.CAPACITY, snapshot.capacity());
        data.putInt(CrystalJadeKeys.LINKS, snapshot.links());
        data.putInt(CrystalJadeKeys.MAX_LINKS, snapshot.maxLinks());
        data.putLong(CrystalJadeKeys.LINK_FLOW, snapshot.outgoingLinkFlow());
        if (snapshot.hasMode()) data.putString(CrystalJadeKeys.MODE, snapshot.mode());
        if (snapshot.hasReceivers()) {
            data.putInt(CrystalJadeKeys.RECEIVERS, snapshot.receivers());
            data.putInt(CrystalJadeKeys.MAX_RECEIVERS, snapshot.maxReceivers());
            data.putLong(CrystalJadeKeys.WIRELESS_FLOW, snapshot.wirelessFlow());
        }
    }
}
