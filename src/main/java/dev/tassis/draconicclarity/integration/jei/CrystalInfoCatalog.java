package dev.tassis.draconicclarity.integration.jei;

import dev.tassis.draconicclarity.crystal.CrystalInformation;
import dev.tassis.draconicclarity.crystal.CrystalTierSpec;
import java.util.List;
import java.util.Optional;

public final class CrystalInfoCatalog {
    public enum Kind {
        DIRECT_IO,
        RELAY,
        WIRELESS
    }

    public record Entry(String itemId, Kind kind, CrystalTierSpec tier) {
        public int maxLinks() {
            return switch (kind) {
                case RELAY -> tier.relayLinks();
                case DIRECT_IO -> tier.directIoLinks();
                case WIRELESS -> tier.wirelessLinks();
            };
        }

        public int maxWirelessTargets() {
            if (kind != Kind.WIRELESS) return 0;
            return switch (tier.tierName()) {
                case "draconium" -> 16;
                case "wyvern" -> 32;
                case "draconic" -> 64;
                default -> throw new IllegalStateException("Unknown crystal tier: " + tier.tierName());
            };
        }

        public long endpointTransferLimit() {
            return switch (kind) {
                case RELAY -> 0;
                case DIRECT_IO -> tier.capacity();
                case WIRELESS -> switch (tier.tierName()) {
                    case "draconium" -> 32_000L;
                    case "wyvern" -> 128_000L;
                    case "draconic" -> 512_000L;
                    default -> throw new IllegalStateException("Unknown crystal tier: " + tier.tierName());
                };
            };
        }
    }

    public static final List<Entry> ENTRIES = List.of(
        new Entry("draconicevolution:basic_io_crystal", Kind.DIRECT_IO, CrystalInformation.DRACONIUM),
        new Entry("draconicevolution:wyvern_io_crystal", Kind.DIRECT_IO, CrystalInformation.WYVERN),
        new Entry("draconicevolution:draconic_io_crystal", Kind.DIRECT_IO, CrystalInformation.DRACONIC),
        new Entry("draconicevolution:basic_relay_crystal", Kind.RELAY, CrystalInformation.DRACONIUM),
        new Entry("draconicevolution:wyvern_relay_crystal", Kind.RELAY, CrystalInformation.WYVERN),
        new Entry("draconicevolution:draconic_relay_crystal", Kind.RELAY, CrystalInformation.DRACONIC),
        new Entry("draconicevolution:basic_wireless_crystal", Kind.WIRELESS, CrystalInformation.DRACONIUM),
        new Entry("draconicevolution:wyvern_wireless_crystal", Kind.WIRELESS, CrystalInformation.WYVERN),
        new Entry("draconicevolution:draconic_wireless_crystal", Kind.WIRELESS, CrystalInformation.DRACONIC)
    );

    private CrystalInfoCatalog() {
    }

    public static List<Entry> entriesFor(Kind kind) {
        return ENTRIES.stream()
            .filter(entry -> entry.kind() == kind)
            .toList();
    }

    public static Optional<Entry> find(String itemId) {
        return ENTRIES.stream().filter(entry -> entry.itemId().equals(itemId)).findFirst();
    }
}
