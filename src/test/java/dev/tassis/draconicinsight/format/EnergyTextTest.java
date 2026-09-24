package dev.tassis.draconicinsight.format;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class EnergyTextTest {
    @Test
    void formatsEnergyWithoutLocaleDependentSeparators() {
        assertEquals("0", EnergyText.compact(0));
        assertEquals("999", EnergyText.compact(999));
        assertEquals("1K", EnergyText.compact(1_000));
        assertEquals("1.5K", EnergyText.compact(1_500));
        assertEquals("4M", EnergyText.compact(4_000_000));
        assertEquals("64M OP", EnergyText.op(64_000_000));
        assertEquals("256M OP/t", EnergyText.opPerTick(256_000_000));
        assertEquals("-1.5K", EnergyText.compact(-1_500));
    }
}
