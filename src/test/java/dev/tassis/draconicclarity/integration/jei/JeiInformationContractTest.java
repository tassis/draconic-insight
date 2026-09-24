package dev.tassis.draconicclarity.integration.jei;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.tassis.draconicclarity.crystal.CrystalInformation;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;

class JeiInformationContractTest {
    @Test
    void everyBaseCrystalAppearsExactlyOnce() {
        assertEquals(9, CrystalInfoCatalog.ENTRIES.size());
        assertEquals(9, new HashSet<>(CrystalInfoCatalog.ENTRIES.stream().map(CrystalInfoCatalog.Entry::itemId).toList()).size());
        assertEquals(new HashSet<>(CrystalInformation.BASE_CRYSTAL_IDS),
            new HashSet<>(CrystalInfoCatalog.ENTRIES.stream().map(CrystalInfoCatalog.Entry::itemId).toList()));
        for (CrystalInfoCatalog.Kind kind : CrystalInfoCatalog.Kind.values()) {
            assertEquals(3, CrystalInfoCatalog.entriesFor(kind).size());
        }
        assertEquals(2, CrystalInfoCatalog.find("draconicevolution:basic_io_crystal").orElseThrow().maxLinks());
        assertEquals(16, CrystalInfoCatalog.find("draconicevolution:basic_wireless_crystal").orElseThrow().maxWirelessTargets());
        assertEquals(32_000L, CrystalInfoCatalog.find("draconicevolution:basic_wireless_crystal").orElseThrow().endpointTransferLimit());
        assertTrue(CrystalInfoCatalog.find("minecraft:air").isEmpty());
    }

    @Test
    void eachInformationPageIsItemSpecificAndSectioned() {
        var entry = CrystalInfoCatalog.find("draconicevolution:basic_wireless_crystal").orElseThrow();
        var lines = CrystalInfoRegistration.informationLines(entry, "jei.draconic_clarity.crystal.wireless");
        assertEquals(4, lines.length);
        assertEquals(Component.translatable("jei.draconic_clarity.crystal.setup.wireless"), lines[2]);
    }

    @Test
    void jeiClassesContainNoRetroReferences() throws IOException {
        assertClassDoesNotContain("CrystalInfoRegistration.class", "retrodraconic");
        assertClassDoesNotContain("DraconicJeiPlugin.class", "retrodraconic");
    }

    private void assertClassDoesNotContain(String className, String forbiddenText) throws IOException {
        String path = "dev/tassis/draconicclarity/integration/jei/" + className;
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(path)) {
            assertTrue(input != null, path);
            String classBytes = new String(input.readAllBytes(), java.nio.charset.StandardCharsets.ISO_8859_1);
            assertFalse(classBytes.contains(forbiddenText));
        }
    }
}
