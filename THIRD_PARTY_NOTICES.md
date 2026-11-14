# Bundled libraries

Draconic Insight includes the following unmodified libraries through NeoForge
Jar-in-Jar. Each nested JAR retains its upstream copyright and complete MIT
license text.

## Ponder

- Project: https://github.com/Creators-of-Create/Ponder
- Copyright (c) 2022 The Create Team
- License: MIT
- Full notice: `LICENSE_Ponder` inside the bundled Ponder JAR.

## Flywheel

- Project: https://github.com/Engine-Room/Flywheel
- Copyright (c) 2021-2024 Jozufozu
- License: MIT
- Full notice: `META-INF/LICENSE.md` inside the bundled Flywheel JAR.

Bundled artifacts and compatible version ranges are recorded in
`META-INF/jarjar/metadata.json`. NeoForge selects one compatible embedded copy
when other mods, such as Create, bundle the same libraries.
