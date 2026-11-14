# Multiblock tutorials

## Player entry points

The Patchouli handbook contains 18 structure diagrams in each of `en_us`,
`zh_tw`, and `zh_cn`. The original explanatory wording is preserved. Long
multiblock captions occupy separate text pages so the diagram's short caption
area does not truncate them. The three oversized introductory spotlight texts
also use full text pages, retaining their original wording and order.
Tier 3 and 4 Energy Cores have additional horizontal
layers, ordered from bottom to top (`↑`).

Ponder and Flywheel are embedded inside the normal Draconic Insight release JAR.
Install Draconic Insight alongside its required Draconic Evolution dependencies,
then hover an associated item and hold the Ponder key (default `W`). No separate
library download or Create installation is required. Patchouli remains optional
and supplies the handbook independently of Ponder.

The earlier Maven-JAR installation instructions have been superseded. Remove
the standalone Ponder 1.0.69 / Flywheel 1.0.0-9 files added through those instructions
when upgrading to the bundled build.

### Bundled distribution

The packaging follows [Create 6.0.10's own Jar-in-Jar setup](https://github.com/Creators-of-Create/Create/blob/mc1.21.1-6.0.10/build.gradle):

| Embedded library | Preferred artifact | Compatible embedded range |
|---|---|---|
| Ponder | `net.createmod.ponder:ponder-neoforge:1.0.82+mc1.21.1` | `[1.0.82+mc1.21.1,2.0)` |
| Flywheel | `dev.engine-room.flywheel:flywheel-neoforge-1.21.1:1.0.6` | `[1.0.0,2.0)` |

These Maven identities match Create's embedded libraries. NeoForge's Jar-in-Jar
selector can choose one compatible embedded copy when both mods are installed.
The release JAR retains the upstream MIT license files inside both nested JARs
and includes `THIRD_PARTY_NOTICES.md`. Publish this normal bundled JAR; no external
Ponder/Flywheel project relationship is needed for users to obtain the libraries.

| Subject | Scenes | Teaching sequence |
|---|---:|---|
| Energy Core | 8 | One shared setup lesson (T1), followed by seven tier-specific shell/layer guides |
| Core stabilizers | 2 | Four axial centers, single blocks versus four perpendicular 3×3 planes, controller validation |
| Energy Pylon | 1 | Pylon placement, full glass on the core-facing side, input/output and core selection |
| Fusion Crafting | 1 | Eight separate inward-facing channels, ingredient/catalyst locations, individual injector power, recipe and tier checks |
| Crystal linking | 1 | Shared Binder selection, linking, unlinking and clearing for all nine crystal items |
| Relay Crystals | 1 | Supply route, extending a network, machine-side output, energy balancing and tier limits |
| I/O Crystals | 1 | Mounted machine face, input supply, network route, empty-hand mode switching and adjacent output |
| Wireless charging | 1 | Input I/O supply, Binder selection/linking, the machine's input face, item charging through a transfuser, reverse mode and tier limits |
| Stabilized spawner | 1 | Convert a vanilla spawner into a dropped item, place it, change Soul/tier, and use redstone to stop spawning |
| Fluid and Flux Gates | 1 | Shared direction/configuration lesson, then fluid and energy rate examples in one scene |
| Reactor | 1 | Four inward Stabilizers, downward Injector above the core, separate input/output Flux Gates, containment supply and startup/shutdown order |

Ponder now uses the actual Energy Core, Crafting Core, and Reactor Core blocks.
Energy and Crafting Cores use their normal block models. A scene-local element
calls DE's transparent renderer for the Reactor Core; global world renderers
are not replaced. The handbook keeps its existing purple-marker diagrams and prose.
Tutorials illustrate operating order and local preview state; they do not run a
live reactor or a complete energy/fluid network simulation.

## Implementation

- `integration/client/TutorialClientSetup` guards both integrations by mod presence
  and runs only on the client. Optional API classes are reached only after the
  corresponding check.
- `integration/patchouli/client/HandbookMultiblocks` registers the two reactor
  previews from the English handbook definitions. Reactor direction is a managed
  block-entity field, not a block-state property; each preview BE is initialized
  to face the core. The pylon and advanced-stabilizer guides additionally recognize
  DE's formed structure placeholders by their original block, so forming a
  multiblock does not make the placement guide reject its glass or stabilizers.
  All locales reference the same registered definitions.
- `integration/ponder/DraconicPonderPlugin` registers item associations and builds
  each scene from the generated catalog, using explicit reactor BE directions.
  Scene scale and vertical offset account for both footprint and height.
- Six indexed Ponder association tags connect Energy Storage, Crystal Network,
  Fusion Crafting, Mob Spawning, Flow Control, and Reactor items. Scene and item
  membership come from the same catalog, so the related-item panel links both
  directions automatically. Flux Gates participate in both Flow Control and Reactor.
- `assets/draconic_insight/ponder/` contains the 19 vanilla structure-template
  NBTs, the geometry/registration catalog, and `groups.json` association icons.
  The catalog contains language-key
  identifiers, not caption text or translated titles.
- Maintain Ponder titles and captions directly in `assets/draconic_insight/lang/`
  (`en_us.json`, `zh_tw.json`, `zh_cn.json`). These are the only text sources;
  the plugin reads English defaults required by Ponder's API from `en_us.json`.
- Maintain handbook text, titles, and pagination directly in each locale's
  `assets/draconic_insight/patchouli_books/draconic_insight/<locale>/` JSON files.

The Patchouli dense pattern convention is `pattern[layer][x][z]`, with layers
listed **top to bottom**. `offset` is added to the position of `0`; it is not an
absolute origin. The pylon's horizontal glass direction and each Fusion
Injector's `facing` must be specified explicitly.

### Rebuilding assets

The generator reads Energy Core shapes from the exact dependency artifact used
by this project, including tiers 5–8. It refuses any other JAR SHA-256:

```text
Curse Maven artifact: draconic-evolution-223565:8797042
DE version: 3.1.4.633
SHA-256: e89f3a23a5478e35099905d7c08f9f6a2ff70289e964904e91f06a7ceeed39b5
```

```sh
python3 scripts/generate_ponder_schematics.py /path/to/draconic-evolution.jar
```

The Python standard library is sufficient. The generator is deterministic and
idempotent and writes only NBT structures, `ponder/tutorials.json`, and
`ponder/groups.json`. It does not contain tutorial prose, generate translations, or read or
  rewrite language files and handbook pages. Text edits require no generator run.

### Manual acceptance for the expanded device tutorials

The expanded 19-scene build is handed to the user for in-game acceptance.
CI runs builds, unit/resource checks, and the bundled-JAR check; GUI and server
smoke runs are optional development tools, not required CI gates.

- Wireless: open from any wireless crystal, the Binder, or Energy Transfuser;
  check selection/link arrows, top-face targeting, item-charging explanation,
  empty-hand sneak switching, and tier limits.
- Relay/I/O: all three tiers have dedicated entry points. Check Binder endpoint
  selection, extending/removing relay links, I/O back-face attachment, and the
  distinction between Binder sneak selection and empty-hand sneak mode switching.
- Associations: click the scene's topic icon to open related items and switch
  to another item's lesson. All crystals share the Binder basics; storage items
  share the T1 setup; both gates share one controls scene.
- Spawner: open from a vanilla/stabilized spawner, Soul, or any of the four
  Draconic cores; check conversion/drop/place, Soul replacement, core swap,
  and the powered lever's stop indication.
- Fluid/Flux Gate: check arrow direction, right-click cue, 0/15 signal settings,
  and the distinction between a passive limiter and a source/pump.
- Cores: check real controller models and the Reactor Core's cold/fuel-loaded
  appearance, including replay and timeline seeking. Crystal FX use Ponder's
  `WrappedClientLevel` facade to satisfy DE's client-world requirement.

## NeoForge compatibility

`neo_version=21.1.249` is the default build pin. `neo_min_version=21.1.228`
independently defines the shipped runtime range `[21.1.228,)`.

JEI 19.51.0.418 requires NeoForge 21.1.238+, independently of Draconic Insight.
The minimum-loader verification therefore uses JEI 19.21.0.247; the existing JEI
integration compiles and runs against both versions. Metadata permits
`[19.21.0.247,20)`. Other installed mods retain their own loader requirements.

CI covers these loader/JEI pairs:

| NeoForge | JEI |
|---|---|
| 21.1.228 | 19.21.0.247 |
| 21.1.249 | 19.51.0.418 |
| 21.1.266 | 19.51.0.418 |

The open runtime range admits later 21.1.x patches; the matrix is the explicit
tested set, not a claim that every future patch has already been exercised.

## Verification commands

Normal checks, including anchor coordinates, inward Fusion directions, upstream
Energy Core geometry, stage coverage, and three-language tutorial keys:

```sh
docker compose run --rm gradle gradle --no-daemon clean test build
docker compose run --rm gradle gradle --no-daemon test build \
  -Pneo_version=21.1.228 -Pjei_version=19.21.0.247
```

Actual client startup, registered reactor BE directions, Ponder storyboard
compilation, structure loading, and registered block/property validation:

```sh
./scripts/verify-tutorials-docker.sh
./scripts/verify-tutorials-docker.sh -Pneo_version=21.1.228 -Pjei_version=19.21.0.247
```

Append `-PwithoutPatchouli` to test the optional handbook's absence.
`-PwithoutPonder` is an internal stripped-development profile for testing the
integration guard; it is not the normal release artifact and must not be published.
The verifier requires a fresh success report and returns failure if
the client reports an error. Reports are
`build/tutorial-smoke-<loader>-<ponder-mode>-<patchouli-mode>.json`.

For an optional integrated-world render pass, including all registered Ponder scenes and
every handbook diagram spread:

```sh
./scripts/verify-tutorials-docker.sh -PtutorialVisualSmoke
```

This mode needs Patchouli and the default bundled Ponder libraries. It creates an isolated disposable world
under `run-tutorial-smoke/`, opens the actual tutorial/book screens, seeks each
Ponder scene to its final instruction stage, and captures images under
`build/tutorial-screens/screenshots/`. Screenshots support visual review of
direction, centering, scaling, and text clipping. They do not establish that a
player has successfully constructed and operated each structure in survival.

The render pass also builds a real pylon and advanced stabilizer plane in the
scratch integrated server. It requires the placement guides to accept both the
raw blocks and DE's formed structure, and to reject a wrong original material.

`src/tools/tutorial-smoke/` is a development-only source set. Ordinary client
and server runs explicitly load only the production mod; the shipping JAR uses
`src/main` plus the two embedded library JARs and contains no smoke-driver classes.

`check` runs `verifyBundledTutorials`, which inspects the actual release artifact
for both embedded libraries, Jar-in-Jar metadata, and their license notices.

Packaged-distribution verification removes loose Ponder/Flywheel JARs and main
output directories from the smoke classpath, then loads the normal release JAR:

```sh
./scripts/verify-tutorials-docker.sh -PpackagedTutorials
./scripts/verify-tutorials-docker.sh -PpackagedTutorials -PwithCreate
docker compose run --rm gradle gradle --no-daemon verifyTutorialServerSmoke -PpackagedTutorials
```

The reports include the production JAR and embedded-library source URIs and
assert exactly one loaded `ponder` and `flywheel` mod. `-PwithCreate` adds the
unmodified Create 6.0.10 distribution only to the verification environment.

### Initial tutorial verification — 2026-11-14

This record describes the initial unbundled 1.0.69 integration. The bundled
distribution uses the newer pins above and is verified separately.

- All 29 JUnit tests passed. The loader matrix above completed builds and actual
  client startup/storyboard-resource checks.
- All four Ponder/Patchouli presence combinations passed on NeoForge 21.1.228
  with JEI 19.21.0.247. Both integrations passed together on 21.1.249 and
  21.1.266 with JEI 19.51.0.418.
- The 21.1.249 integrated-world run rendered all 13 Ponder scenes and captured
  31 screenshots covering those scenes, every handbook diagram spread, and the
  introductory spreads. Review caught and corrected scene clipping, untranslated
  layer headings, and truncated existing explanations.
- Both real pylon and advanced-stabilizer placement guides accepted the raw and
  formed structures and rejected the wrong-material control.
- Existing handbook text strings and their reading order matched commit
  `3957928` exactly; all 288 original page strings and existing language values
  were retained. Asset regeneration was byte-for-byte idempotent.
- The shipping JAR contained 13 schematics, the `[21.1.228,)` loader range, and
  no smoke-driver classes or bundled Ponder/Patchouli APIs. Both workflow files
  passed `actionlint`.

### Bundled-distribution verification — 2026-11-14

- Updated to the same Ponder 1.0.82+mc1.21.1 / Flywheel 1.0.6 artifacts embedded
  by Create 6.0.10. The normal release JAR includes both libraries, compatible
  Jar-in-Jar ranges, their original MIT licenses, and the third-party notices.
- Actual release-JAR client and dedicated-server loading passed on NeoForge
  21.1.228, 21.1.249, and 21.1.266. Runtime source URIs confirmed that the libraries
  came from nested JARs, with loose development copies removed from the classpath.
- The packaged mod also passed client and dedicated-server loading with Create
  6.0.10 on both 21.1.228 and 21.1.249. Exactly one Ponder and one Flywheel mod were
  loaded; runtime URIs showed NeoForge selecting shared embedded copies.
- The packaged 13-scene render pass passed both with and without Create, including
  both real formed-placement guide checks and all 31 screenshots in each run.
- Packaged loading without Patchouli and stripped-development integration guards
  passed on 21.1.228. All 29 JUnit tests and the bundled-artifact checks passed.
