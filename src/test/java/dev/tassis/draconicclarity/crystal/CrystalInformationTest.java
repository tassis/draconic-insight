package dev.tassis.draconicclarity.crystal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class CrystalInformationTest {
    @Test
    void tierSpecificationsMatchTheLockedImplementations() {
        assertEquals(4_000_000L, CrystalInformation.DRACONIUM.capacity());
        assertEquals(16_000_000L, CrystalInformation.WYVERN.capacity());
        assertEquals(64_000_000L, CrystalInformation.DRACONIC.capacity());

        assertEquals(8, CrystalInformation.DRACONIUM.relayLinks());
        assertEquals(32, CrystalInformation.DRACONIC.relayLinks());
        assertEquals(9, CrystalInformation.BASE_CRYSTAL_IDS.size());
    }

    @Test
    void tierOrderingAndNamesAreUnique() {
        assertEquals(
            Set.of("draconium", "wyvern", "draconic"),
            CrystalInformation.ALL_TIERS.stream().map(CrystalTierSpec::tierName).collect(Collectors.toSet())
        );
        for (int index = 1; index < CrystalInformation.ALL_TIERS.size(); index++) {
            assertTrue(CrystalInformation.ALL_TIERS.get(index).capacity()
                > CrystalInformation.ALL_TIERS.get(index - 1).capacity());
        }
    }

    @Test
    void englishAndTraditionalChineseExposeTheSameContractKeys() {
        JsonObject english = readLanguage("en_us");
        JsonObject traditionalChinese = readLanguage("zh_tw");
        assertEquals(english.keySet(), traditionalChinese.keySet());
        assertTrue(english.keySet().contains("jei.draconic_clarity.crystal.limits.relay"));
        assertTrue(english.keySet().contains("jei.draconic_clarity.binder.select"));
        assertTrue(english.keySet().contains("jade.draconic_clarity.flow"));
        assertTrue(english.keySet().contains("jade.draconic_clarity.facility.core"));
        assertTrue(english.keySet().contains("jade.draconic_clarity.facility.charge"));
    }

    private JsonObject readLanguage(String locale) {
        var stream = getClass().getClassLoader().getResourceAsStream(
            "assets/draconic_clarity/lang/" + locale + ".json"
        );
        assertNotNull(stream);
        return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
    }
}
