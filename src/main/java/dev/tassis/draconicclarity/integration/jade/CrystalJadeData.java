package dev.tassis.draconicclarity.integration.jade;

import net.minecraft.nbt.CompoundTag;

public final class CrystalJadeData {
    private CrystalJadeData() {
    }

    public static void write(CompoundTag data, CrystalSnapshot snapshot) {
        data.putLong(CrystalJadeKeys.ENERGY, snapshot.energy());
        data.putLong(CrystalJadeKeys.CAPACITY, snapshot.capacity());
        data.putDouble(CrystalJadeKeys.CHARGE, snapshot.chargePercent());
        data.putInt(CrystalJadeKeys.TIER, snapshot.tier());
        data.putString(CrystalJadeKeys.TYPE, snapshot.type());
        data.putInt(CrystalJadeKeys.LINKS, snapshot.links());
        data.putInt(CrystalJadeKeys.MAX_LINKS, snapshot.maxLinks());
        data.putInt(CrystalJadeKeys.RANGE, snapshot.range());
        data.putLong(CrystalJadeKeys.LINK_FLOW, snapshot.outgoingLinkFlow());
        if (snapshot.hasMode()) data.putString(CrystalJadeKeys.MODE, snapshot.mode());
        if (snapshot.hasReceivers()) {
            data.putInt(CrystalJadeKeys.RECEIVERS, snapshot.receivers());
            data.putInt(CrystalJadeKeys.MAX_RECEIVERS, snapshot.maxReceivers());
            data.putLong(CrystalJadeKeys.WIRELESS_FLOW, snapshot.wirelessFlow());
        }
    }
}
