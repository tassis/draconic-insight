package dev.tassis.draconicclarity.integration.jade;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CrystalJadeDefaultsTest {
    @Test
    void duplicateCrystalDetailsAreOptIn() throws ReflectiveOperationException {
        Class<?> providerClass = Class.forName(
            "dev.tassis.draconicclarity.integration.jade.client.CrystalComponentProvider"
        );
        Object provider = providerClass.getField("INSTANCE").get(null);
        assertFalse((boolean) providerClass.getMethod("enabledByDefault").invoke(provider));
        assertEquals("draconic_clarity:crystal_flow", providerClass.getField("FLOW_CONFIG").get(null).toString());
    }
}
