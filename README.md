# Draconic Insight

[![CI](https://github.com/tassis/draconic-insight/actions/workflows/build.yml/badge.svg)](https://github.com/tassis/draconic-insight/actions/workflows/build.yml)

Independent NeoForge `1.21.1` information companion for Draconic Evolution.

The mod keeps four concerns separate:

- Patchouli provides a craftable bilingual handbook for progression, structures, energy systems, Fusion Crafting, and modular equipment.
- JEI provides searchable, localized static explanations.
- EMI receives JEI information through JEMI when that bridge supports the information recipe type.
- Jade provides server-authoritative live facility diagnostics. Crystal details remain available as an opt-in Jade setting, disabled by default because Draconic Evolution already supplies a native crystal HUD. Recent crystal power-flow telemetry has its own setting and is also disabled by default.

Draconic Evolution is required; Patchouli, JEI, and Jade are optional integrations. The handbook is crafted shapelessly from one Book and one Draconium Dust when Patchouli is installed. Historical Retro Draconic support is outside the active runtime and acceptance scope. The mod does not change energy transfer, recipes, equipment balance, or progression.

The technical mod ID and resource namespace are `draconic_insight`; the Java package is `dev.tassis.draconicinsight`.

## Build

GitHub Actions runs the full test and build pipeline for branch pushes, pull requests, and manual CI runs. CI uploads test reports on failure; release JARs are built and uploaded by the separate Release workflow.

The local Docker build uses the same Gradle and Java versions as CI:

```bash
docker compose run --rm gradle gradle --no-daemon clean test build
```

The output JAR is written to `build/libs/draconic_insight-neoforge-1.21.1-<mod_version>.jar`, using the default version in `gradle.properties`.

## Releases

Start releases from **Actions → Release → Run workflow**, select `main`, and enter `release_version` without a `v` prefix (for example `0.3.0` or `0.3.0-rc.1`). The equivalent GitHub CLI command is:

```bash
gh workflow run release.yml --ref main -f release_version=0.3.0
```

The workflow:

1. Requires `main` and validates the requested version and any existing tag/Release.
2. Runs `gradle --no-daemon clean test build -Pmod_version=<release_version>`. The input controls both the JAR filename and embedded mod version; it does not rewrite `gradle.properties` or create a version commit.
3. Uploads the JAR and its SHA-256 checksum as an Actions artifact, then creates the annotated `v<release_version>` tag at the exact commit built.
4. Creates a draft GitHub Release with generated release notes, uploads both files, downloads them to verify byte-for-byte equality, and publishes the Release. Hyphenated versions are marked as prereleases.

Releases are serialized and use the `release` GitHub environment. Configure that environment's deployment branch rule to allow only `main`; add required reviewers there if desired. The release job requires `contents: write` through `GITHUB_TOKEN` and permission to create `v*` tags under any repository tag rulesets.

If publication fails after tag or draft creation, rerun the same workflow run for the same commit/version. An existing annotated tag must point directly to that commit. Draft assets may be replaced to finish publication; an already published Release is only verified against the rebuilt files and metadata. Conflicts fail rather than moving tags or replacing published assets. Manually pushing a tag no longer starts publication.

## License

Draconic Insight is available under the [MIT License](LICENSE).
