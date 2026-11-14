#!/bin/sh
set -eu

ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
docker build -t draconic-insight-tutorial-smoke -f "$ROOT/scripts/Dockerfile.tutorial-smoke" "$ROOT"
docker run --rm --init \
    -e LIBGL_ALWAYS_SOFTWARE=1 -e ALSOFT_DRIVERS=null \
    -v "$ROOT:/workspace" \
    -v draconic-insight-verification-gradle:/home/gradle/.gradle \
    draconic-insight-tutorial-smoke \
    xvfb-run -a -s '-screen 0 1280x800x24' \
    gradle --no-daemon verifyTutorialSmoke "$@"
