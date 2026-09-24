# Draconic Insight

[![Build](https://github.com/tassis/draconic-insight/actions/workflows/build.yml/badge.svg)](https://github.com/tassis/draconic-insight/actions/workflows/build.yml)

Independent NeoForge `1.21.1` information companion for Draconic Evolution.

The mod keeps four concerns separate:

- Patchouli provides a craftable bilingual handbook for progression, structures, energy systems, Fusion Crafting, and modular equipment.
- JEI provides searchable, localized static explanations.
- EMI receives JEI information through JEMI when that bridge supports the information recipe type.
- Jade provides server-authoritative live facility diagnostics. Crystal details remain available as an opt-in Jade setting, disabled by default because Draconic Evolution already supplies a native crystal HUD. Recent crystal power-flow telemetry has its own setting and is also disabled by default.

Draconic Evolution is required; Patchouli, JEI, and Jade are optional integrations. The handbook is crafted shapelessly from one Book and one Draconium Dust when Patchouli is installed. Historical Retro Draconic support is outside the active runtime and acceptance scope. The mod does not change energy transfer, recipes, equipment balance, or progression.

The technical mod ID and resource namespace are `draconic_insight`; the Java package is `dev.tassis.draconicinsight`.

## Build

GitHub Actions runs the full test and build pipeline for every push and pull request, then uploads a JAR whose name identifies the mod loader, Minecraft version, and mod version.

The local Docker build uses the same Gradle and Java versions as CI:

```bash
docker compose run --rm gradle gradle --no-daemon clean test build
```

The output JAR is written to `build/libs/draconic_insight-neoforge-1.21.1-0.1.0.jar`.

## Releases

Stable releases use annotated semantic-version tags with a `v` prefix. The tag must match `mod_version` in `gradle.properties`; for example, version `0.1.0` must use tag `v0.1.0`.

```bash
git tag -a v0.1.0 -m "Draconic Insight 0.1.0"
git push origin v0.1.0
```

A matching tag runs the full test/build pipeline, creates a GitHub Release named `Draconic Insight 0.1.0`, generates release notes, and attaches the versioned JAR. A mismatched tag fails before publication.

## License

Draconic Insight is available under the [MIT License](LICENSE).
