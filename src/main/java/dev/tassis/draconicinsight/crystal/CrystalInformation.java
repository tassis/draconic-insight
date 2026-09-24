package dev.tassis.draconicinsight.crystal;

import java.util.List;
import java.util.Map;

public final class CrystalInformation {
    public static final String DRACONIC_EVOLUTION_VERSION = "3.1.4.633";

    public static final CrystalTierSpec DRACONIUM = new CrystalTierSpec(
        "draconium", "draconicevolution", DRACONIC_EVOLUTION_VERSION,
        4_000_000L, 8, 2, 4, 32
    );
    public static final CrystalTierSpec WYVERN = new CrystalTierSpec(
        "wyvern", "draconicevolution", DRACONIC_EVOLUTION_VERSION,
        16_000_000L, 16, 3, 8, 64
    );
    public static final CrystalTierSpec DRACONIC = new CrystalTierSpec(
        "draconic", "draconicevolution", DRACONIC_EVOLUTION_VERSION,
        64_000_000L, 32, 4, 16, 127
    );
    public static final List<CrystalTierSpec> BASE_TIERS = List.of(DRACONIUM, WYVERN, DRACONIC);
    public static final List<CrystalTierSpec> ALL_TIERS = BASE_TIERS;

    public static final Map<String, CrystalTierSpec> BY_TIER_NAME = Map.of(
        DRACONIUM.tierName(), DRACONIUM,
        WYVERN.tierName(), WYVERN,
        DRACONIC.tierName(), DRACONIC
    );

    public static final List<String> BASE_CRYSTAL_IDS = List.of(
        "draconicevolution:basic_io_crystal",
        "draconicevolution:wyvern_io_crystal",
        "draconicevolution:draconic_io_crystal",
        "draconicevolution:basic_relay_crystal",
        "draconicevolution:wyvern_relay_crystal",
        "draconicevolution:draconic_relay_crystal",
        "draconicevolution:basic_wireless_crystal",
        "draconicevolution:wyvern_wireless_crystal",
        "draconicevolution:draconic_wireless_crystal"
    );

    private CrystalInformation() {
    }
}
