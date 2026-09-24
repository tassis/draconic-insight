# Draconic Clarity

Independent NeoForge `1.21.1` information integration for the locked Draconic Evolution stack in Traveler: Echoes.

The mod keeps three concerns separate:

- JEI provides searchable, localized static explanations.
- EMI receives JEI information through JEMI when that bridge supports the information recipe type.
- Jade provides server-authoritative live machine and crystal diagnostics.

Draconic Evolution is required; JEI and Jade are optional integrations. Historical Retro Draconic support is outside the active runtime and acceptance scope. The mod does not change energy transfer, recipes, equipment balance, or progression.

## Docker build

```bash
docker compose run --rm gradle gradle --no-daemon clean test build
```

The output JAR is written under `build/libs/`. Development artifacts must be staged through the explicit Packwiz local-artifact process and remain release-blocked until they have an approved publication path.
