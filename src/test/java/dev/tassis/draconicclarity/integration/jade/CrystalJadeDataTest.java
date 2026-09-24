package dev.tassis.draconicclarity.integration.jade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

class CrystalJadeDataTest {
    @Test
    void serializesAFullWirelessSnapshotWithoutLosingLongValues() {
        CrystalSnapshot snapshot = new CrystalSnapshot(
            128_000_000L, 256_000_000L, 12, 32, 4_294_967_294L,
            "input", 5, 32, 512_000L
        );
        CompoundTag data = new CompoundTag();
        CrystalJadeData.write(data, snapshot);

        assertEquals(128_000_000L, data.getLong(CrystalJadeKeys.ENERGY));
        assertEquals(256_000_000L, data.getLong(CrystalJadeKeys.CAPACITY));
        assertEquals(4_294_967_294L, data.getLong(CrystalJadeKeys.LINK_FLOW));
        assertEquals("input", data.getString(CrystalJadeKeys.MODE));
        assertEquals(5, data.getInt(CrystalJadeKeys.RECEIVERS));
        assertEquals(512_000L, data.getLong(CrystalJadeKeys.WIRELESS_FLOW));
    }

    @Test
    void relaySnapshotOmitsModeAndWirelessFields() {
        CompoundTag data = new CompoundTag();
        CrystalJadeData.write(data, new CrystalSnapshot(
            0, 4_000_000L, 0, 8, 0, "", 0, 0, 0
        ));
        assertFalse(data.contains(CrystalJadeKeys.MODE));
        assertFalse(data.contains(CrystalJadeKeys.RECEIVERS));
        assertFalse(data.contains("Charge"));
        assertFalse(data.contains("Range"));
    }

    @Test
    void rejectsImpossibleState() {
        assertThrows(IllegalArgumentException.class, () -> new CrystalSnapshot(
            5, 4, 0, 8, 0, "", 0, 0, 0
        ));
        assertThrows(IllegalArgumentException.class, () -> new CrystalSnapshot(
            0, 4, 9, 8, 0, "", 0, 0, 0
        ));
        assertFalse(new CrystalSnapshot(2, 4, 0, 8, 0, "", 0, 0, 0).hasReceivers());
    }
}
