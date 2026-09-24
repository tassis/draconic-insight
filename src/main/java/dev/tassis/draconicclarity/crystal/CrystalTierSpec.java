package dev.tassis.draconicclarity.crystal;

public record CrystalTierSpec(
    String tierName,
    String sourceMod,
    String sourceVersion,
    long capacity,
    int relayLinks,
    int directIoLinks,
    int wirelessLinks,
    int range
) {
    public CrystalTierSpec {
        if (tierName == null || tierName.isBlank()) throw new IllegalArgumentException("tierName");
        if (sourceMod == null || sourceMod.isBlank()) throw new IllegalArgumentException("sourceMod");
        if (sourceVersion == null || sourceVersion.isBlank()) throw new IllegalArgumentException("sourceVersion");
        if (capacity <= 0) throw new IllegalArgumentException("capacity");
        if (relayLinks <= 0 || directIoLinks <= 0 || wirelessLinks <= 0) {
            throw new IllegalArgumentException("link limits must be positive");
        }
        if (range <= 0) throw new IllegalArgumentException("range");
    }
}
