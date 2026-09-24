package dev.tassis.draconicclarity.integration.jade;

public record CrystalSnapshot(
    long energy,
    long capacity,
    int tier,
    String type,
    int links,
    int maxLinks,
    int range,
    long outgoingLinkFlow,
    String mode,
    int receivers,
    int maxReceivers,
    long wirelessFlow
) {
    public CrystalSnapshot {
        if (energy < 0 || capacity <= 0 || energy > capacity) throw new IllegalArgumentException("energy");
        if (tier < 0 || type == null || type.isBlank()) throw new IllegalArgumentException("type/tier");
        if (links < 0 || maxLinks < links || range <= 0) throw new IllegalArgumentException("links/range");
        if (outgoingLinkFlow < 0 || wirelessFlow < 0) throw new IllegalArgumentException("flow");
        if (receivers < 0 || maxReceivers < receivers) throw new IllegalArgumentException("receivers");
        mode = mode == null ? "" : mode;
    }

    public double chargePercent() {
        return ((double) energy / (double) capacity) * 100D;
    }

    public boolean hasMode() {
        return !mode.isEmpty();
    }

    public boolean hasReceivers() {
        return maxReceivers > 0;
    }
}
