package dev.tassis.draconicinsight.integration.jade;

public record CrystalSnapshot(
    long energy,
    long capacity,
    int links,
    int maxLinks,
    long outgoingLinkFlow,
    String mode,
    int receivers,
    int maxReceivers,
    long wirelessFlow
) {
    public CrystalSnapshot {
        if (energy < 0 || capacity <= 0 || energy > capacity) throw new IllegalArgumentException("energy");
        if (links < 0 || maxLinks < links) throw new IllegalArgumentException("links");
        if (outgoingLinkFlow < 0 || wirelessFlow < 0) throw new IllegalArgumentException("flow");
        if (receivers < 0 || maxReceivers < receivers) throw new IllegalArgumentException("receivers");
        mode = mode == null ? "" : mode;
    }

    public boolean hasMode() {
        return !mode.isEmpty();
    }

    public boolean hasReceivers() {
        return maxReceivers > 0;
    }
}
