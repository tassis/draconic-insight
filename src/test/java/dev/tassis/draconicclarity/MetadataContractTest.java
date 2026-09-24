package dev.tassis.draconicclarity;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class MetadataContractTest {
    @Test
    void metadataKeepsTheIntegrationIndependentAndViewersOptional() throws IOException {
        var resources = Collections.list(getClass().getClassLoader()
            .getResources("META-INF/neoforge.mods.toml"));
        String metadata = null;
        for (var resource : resources) {
            try (InputStream input = resource.openStream()) {
                String candidate = new String(input.readAllBytes(), StandardCharsets.UTF_8);
                if (candidate.contains("modId=\"draconic_clarity\"")) {
                    metadata = candidate;
                    break;
                }
            }
        }

        assertNotNull(metadata);
        assertTrue(metadata.contains("modId=\"draconicevolution\"\ntype=\"required\""));
        assertTrue(metadata.contains("modId=\"jei\"\ntype=\"optional\""));
        assertTrue(metadata.contains("modId=\"jade\"\ntype=\"optional\""));
        assertFalse(metadata.contains("modId=\"emi\""));
        assertFalse(metadata.contains("retrodraconic"));
        assertFalse(metadata.contains("modId=\"traveler_echoes\""));
    }
}
