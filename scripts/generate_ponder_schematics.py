#!/usr/bin/env python3
"""Rebuild Ponder structures and geometry catalog from the pinned DE JAR."""
import argparse
import gzip
import hashlib
import json
from pathlib import Path
import struct
import zipfile

ROOT = Path(__file__).resolve().parents[1]
OUTPUT = ROOT / "src/main/resources/assets/draconic_insight/ponder"
DE_SHA256 = "e89f3a23a5478e35099905d7c08f9f6a2ff70289e964904e91f06a7ceeed39b5"
DE = "draconicevolution:"
ENERGY_CORE = DE + "energy_core"
GROUP_ICONS = {
    "energy_storage": "energy_core",
    "crystal_network": "crystal_binder",
    "fusion_crafting": "crafting_core",
    "spawning": "stabilized_spawner",
    "flow_control": "fluid_gate",
    "reactor": "reactor_core",
}


def string(value):
    data = value.encode("utf-8")
    return struct.pack(">H", len(data)) + data


def payload(kind, value):
    if kind == 3:
        return struct.pack(">i", value)
    if kind == 8:
        return string(value)
    if kind == 9:
        child_kind, children = value
        return bytes([child_kind]) + struct.pack(">i", len(children)) + b"".join(payload(child_kind, c) for c in children)
    if kind == 10:
        return b"".join(bytes([t]) + string(k) + payload(t, v) for k, (t, v) in value.items()) + b"\0"
    raise ValueError(kind)


def state(name):
    block, _, properties = name.partition("[")
    result = {"Name": (8, block)}
    if properties:
        result["Properties"] = (10, {k: (8, v) for k, v in (p.split("=") for p in properties.rstrip("]").split(","))})
    return result


def write_structure(path, size, blocks):
    palette = sorted(set(blocks.values()))
    indices = {name: i for i, name in enumerate(palette)}
    entries = [{"pos": (9, (3, list(pos))), "state": (3, indices[name])} for pos, name in sorted(blocks.items())]
    root = {"DataVersion": (3, 3955), "size": (9, (3, size)),
            "palette": (9, (10, [state(name) for name in palette])),
            "blocks": (9, (10, entries)), "entities": (9, (10, []))}
    path.write_bytes(gzip.compress(b"\x0a\x00\x00" + payload(10, root), mtime=0))


def guide(name, items, width, height, core):
    if name.startswith("energy_core_") or name in ("stabilizers", "advanced_stabilizers", "energy_pylon"):
        group = "energy_storage"
    elif name in ("crystal_linking", "relay_crystals", "direct_io", "wireless_charging"):
        group = "crystal_network"
    elif name == "stabilized_spawner":
        group = "spawning"
    elif name == "flow_gates":
        group = "flow_control"
    else:
        group = name
    return {"id": name, "items": [i if ":" in i else DE + i for i in items],
            "group": group, "size": [width, height, width], "core": list(core), "stages": [], "facings": []}


def stage(g, caption, positions, **options):
    positions = [list(p) for p in positions]
    if positions:
        g["stages"].append({"caption": caption, "positions": positions, **options})


def base(width):
    return {(x, 0, z): "minecraft:smooth_stone" for x in range(width) for z in range(width)}


def stabilizers(blocks, core, distance, advanced):
    result = []
    cx, cy, cz = core
    for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1)):
        for a in range(-1, 2) if advanced else (0,):
            for b in range(-1, 2) if advanced else (0,):
                pos = (cx + dx * distance + (a if dz else 0), cy + b, cz + dz * distance + (a if dx else 0))
                blocks[pos] = DE + "energy_core_stabilizer"
                result.append(pos)
    return result


def generate(jar):
    OUTPUT.mkdir(parents=True, exist_ok=True)
    guides = []
    for tier in range(1, 9):
        definition = json.loads(jar.read(f"data/draconicevolution/multiblocks/energy_core_{tier}.json"))
        r = len(definition["structure"]) // 2
        d = max(4, r + 3)
        width = 2 * d + 3
        core = (d + 1, r + 2, d + 1)
        g = guide(f"energy_core_{tier}", ["energy_core"], width, 2 * r + 5, core)
        blocks = base(width)
        origin = definition.get("origin", {"x": 0, "y": 0, "z": 0})
        interior, layers = [], {}
        material = {"D": DE + "draconium_block", "R": "minecraft:redstone_block", "A": DE + "awakened_draconium_block", "X": ENERGY_CORE}
        for y, layer in enumerate(definition["structure"]):
            for x, row in enumerate(layer):
                for z, c in enumerate(row):
                    if c == " ":
                        continue
                    pos = (core[0] + x - origin["x"], core[1] + len(definition["structure"]) - y - 1 - origin["y"], core[2] + z - origin["z"])
                    blocks[pos] = material[c]
                    if c == "R" or (tier == 8 and c == "D"):
                        interior.append(pos)
                    elif c in "DA":
                        layers.setdefault(pos[1], []).append(pos)
        centers = stabilizers(blocks, core, d, tier >= 5)
        if tier == 1:
            g["items"] += [DE + "energy_core_stabilizer", DE + "energy_pylon"]
            stage(g, "core_marker", [core])
        else:
            g["initial"] = [list(p) for p in [core, *centers]]
        stage(g, "core_interior", interior)
        for number, y in enumerate(sorted(layers), 1):
            stage(g, "core_layer_number", layers[y], arguments=[str(number), str(len(layers))])
        if tier == 1:
            stage(g, "core_stabilizers", centers)
            stage(g, "core_use", [core])
        else:
            stage(g, "core_shell_ready", [core])
        write_structure(OUTPUT / (g["id"] + ".nbt"), g["size"], blocks)
        guides.append(g)

    for advanced in (False, True):
        core = (6, 3, 6)
        g = guide("advanced_stabilizers" if advanced else "stabilizers", ["energy_core_stabilizer"], 13, 6, core)
        blocks = base(13)
        blocks[core] = ENERGY_CORE
        stage(g, "stabilizer_marker", [core])
        stage(g, "stabilizer_plane" if advanced else "stabilizer_single", stabilizers(blocks, core, 4, advanced))
        stage(g, "stabilizer_check" if advanced else "stabilizer_check_single", [core])
        write_structure(OUTPUT / (g["id"] + ".nbt"), g["size"], blocks)
        guides.append(g)

    core = (7, 2, 4)
    g = guide("energy_pylon", ["energy_pylon"], 9, 5, core)
    blocks = base(9)
    blocks.update({core: ENERGY_CORE, (2, 2, 4): DE + "energy_pylon[facing=east]", (3, 2, 4): "minecraft:glass"})
    stage(g, "pylon_place", [core, (2, 2, 4)])
    stage(g, "pylon_glass", [(3, 2, 4)])
    stage(g, "pylon_use", [(2, 2, 4)])
    write_structure(OUTPUT / (g["id"] + ".nbt"), g["size"], blocks)
    guides.append(g)

    core = (5, 2, 5)
    g = guide("fusion_crafting", ["crafting_core", "basic_crafting_injector", "wyvern_crafting_injector",
                                "draconic_crafting_injector", "chaotic_crafting_injector"], 11, 5, core)
    blocks = base(11)
    blocks[core] = DE + "crafting_core"
    positions = []
    for dx, dz, facing in ((-3, 0, "east"), (3, 0, "west"), (0, -3, "south"), (0, 3, "north")):
        for lateral in (-1, 1):
            pos = (5 + dx + (lateral if dz else 0), 2, 5 + dz + (lateral if dx else 0))
            blocks[pos] = DE + "basic_crafting_injector[facing=" + facing + "]"
            positions.append(pos)
    stage(g, "fusion_marker", [core])
    stage(g, "fusion_channels", positions)
    stage(g, "fusion_power", positions)
    stage(g, "fusion_use", [core])
    write_structure(OUTPUT / (g["id"] + ".nbt"), g["size"], blocks)
    guides.append(g)

    # Positions retain their semantic order: clicked subject first, linked
    # partner second. Captions are language-key identifiers, not prose.
    core, target = (2, 1, 3), (4, 1, 3)
    crystals = [tier + "_" + kind + "_crystal" for kind in ("relay", "io", "wireless")
                for tier in ("basic", "wyvern", "draconic")]
    g = guide("crystal_linking", [*crystals, "crystal_binder"], 7, 4, core)
    blocks = base(7)
    blocks.update({core: DE + "basic_relay_crystal", target: DE + "basic_relay_crystal"})
    stage(g, "binding_select", [core, target], item=DE + "crystal_binder", sneak=True)
    stage(g, "binding_connect", [target, core], item=DE + "crystal_binder", action="network_supply")
    stage(g, "binding_unlink", [target, core], item=DE + "crystal_binder")
    stage(g, "binding_clear", [core], item=DE + "crystal_binder", sneak=True, action="binding_clear")
    stage(g, "binding_limits", [core, target])
    write_structure(OUTPUT / (g["id"] + ".nbt"), g["size"], blocks)
    guides.append(g)

    source, io, core, relay, output, receiver = (1, 1, 5), (2, 1, 5), (4, 1, 5), (6, 1, 5), (8, 2, 5), (8, 1, 5)
    g = guide("relay_crystals", ["basic_relay_crystal", "wyvern_relay_crystal", "draconic_relay_crystal",
                                "crystal_binder"], 11, 5, core)
    blocks = base(11)
    blocks.update({source: DE + "generator[facing=north,active=true]", io: DE + "basic_io_crystal",
                   core: DE + "basic_relay_crystal", relay: DE + "basic_relay_crystal",
                   output: DE + "basic_io_crystal", receiver: DE + "energy_transfuser"})
    g["devices"] = [{"kind": "io_input", "pos": list(io), "direction": "west"},
                    {"kind": "io_output", "pos": list(output), "direction": "down"},
                    {"kind": "transfuser", "pos": list(receiver), "item": DE + "wyvern_capacitor"}]
    stage(g, "relay_supply", [source, io, core])
    stage(g, "relay_route", [io, core, relay, output], action="crystal_route")
    stage(g, "relay_load", [output, receiver], action="io_delivery")
    stage(g, "relay_balance", [core, relay])
    stage(g, "relay_limits", [core, relay])
    write_structure(OUTPUT / (g["id"] + ".nbt"), g["size"], blocks)
    guides.append(g)

    source, core, relay, output, receiver = (1, 1, 4), (2, 1, 4), (4, 1, 4), (6, 2, 4), (6, 1, 4)
    g = guide("direct_io", ["basic_io_crystal", "wyvern_io_crystal", "draconic_io_crystal",
                           "crystal_binder", "energy_transfuser"], 9, 5, core)
    blocks = base(9)
    blocks.update({source: DE + "generator[facing=north,active=true]", core: DE + "basic_io_crystal",
                   relay: DE + "basic_relay_crystal", output: DE + "basic_io_crystal",
                   receiver: DE + "energy_transfuser"})
    g["devices"] = [{"kind": "io_input", "pos": list(core), "direction": "west"},
                    {"kind": "io_input", "pos": list(output), "direction": "down"},
                    {"kind": "transfuser", "pos": list(receiver), "item": DE + "wyvern_capacitor"}]
    stage(g, "io_mount", [source, core], item=DE + "basic_io_crystal", direction="east")
    stage(g, "io_input", [core, source])
    stage(g, "io_network", [core, relay, output], action="crystal_route")
    stage(g, "io_output_link", [output, receiver])
    stage(g, "io_switch", [output], sneak=True, action="io_output")
    stage(g, "io_delivery", [output, receiver], action="io_delivery")
    stage(g, "io_limits", [core, output])
    write_structure(OUTPUT / (g["id"] + ".nbt"), g["size"], blocks)
    guides.append(g)

    core = (4, 1, 4)
    source, io, receiver = (1, 1, 4), (2, 1, 4), (7, 1, 4)
    g = guide("wireless_charging", ["basic_wireless_crystal", "wyvern_wireless_crystal",
                                   "draconic_wireless_crystal", "crystal_binder", "energy_transfuser"], 9, 4, core)
    blocks = base(9)
    blocks.update({source: DE + "generator[facing=north,active=true]", io: DE + "basic_io_crystal",
                   core: DE + "basic_wireless_crystal", receiver: DE + "energy_transfuser"})
    g["devices"] = [{"kind": "io_input", "pos": list(io), "direction": "west"},
                    {"kind": "transfuser", "pos": list(receiver), "item": DE + "wyvern_capacitor"}]
    stage(g, "wireless_source", [source, io], item="minecraft:coal")
    stage(g, "wireless_select", [core], item=DE + "crystal_binder", sneak=True)
    stage(g, "wireless_network", [io, core], item=DE + "crystal_binder", action="network_link")
    stage(g, "wireless_receiver", [receiver, core], item=DE + "crystal_binder", action="receiver_link", direction="up")
    stage(g, "wireless_items", [receiver], item=DE + "wyvern_capacitor")
    stage(g, "wireless_reverse", [core], sneak=True, action="wireless_input")
    stage(g, "wireless_limits", [core, receiver])
    write_structure(OUTPUT / (g["id"] + ".nbt"), g["size"], blocks)
    guides.append(g)

    core, lever = (4, 1, 4), (5, 1, 4)
    g = guide("stabilized_spawner", ["minecraft:spawner", "stabilized_spawner", "mob_soul", "draconium_core",
                                    "wyvern_core", "awakened_core", "chaotic_core"], 9, 5, core)
    blocks = base(9)
    blocks.update({core: "minecraft:spawner", lever: "minecraft:lever[face=floor,facing=north,powered=false]"})
    g["devices"] = [{"kind": "vanilla_spawner", "pos": list(core)}]
    stage(g, "spawner_vanilla", [core])
    stage(g, "spawner_convert", [core], item=DE + "draconium_core", action="spawner_convert")
    stage(g, "spawner_place", [core], item=DE + "stabilized_spawner", action="spawner_place")
    stage(g, "spawner_soul", [core], item=DE + "mob_soul", action="spawner_soul")
    stage(g, "spawner_upgrade", [core], item=DE + "wyvern_core", action="spawner_upgrade")
    stage(g, "spawner_stop", [lever, core], action="spawner_stop")
    stage(g, "spawner_checks", [core, lever])
    write_structure(OUTPUT / (g["id"] + ".nbt"), g["size"], blocks)
    guides.append(g)

    core, flux = (3, 1, 2), (3, 1, 5)
    g = guide("flow_gates", ["fluid_gate", "flux_gate"], 7, 4, core)
    blocks = base(7)
    g["devices"] = []
    pairs = [("fluid_gate", core, (3, 1, 1)), ("flux_gate", flux, (3, 1, 4))]
    for kind, position, lever in pairs:
        blocks[position] = DE + kind + "[facing=east]"
        blocks[lever] = "minecraft:lever[face=floor,facing=north,powered=false]"
        g["devices"].append({"kind": "flow_gate", "pos": list(position), "minimum": 100, "maximum": 1000})
    g["initial"] = [list(p) for _, position, lever in pairs for p in (position, lever)]
    stage(g, "gate_direction", [core, flux], action="gate_direction")
    stage(g, "gate_configure", [core, flux], action="gate_configure")
    for kind, position, lever in pairs:
        stage(g, kind + "_low", [position, lever], signal=0, arguments=["100"])
        stage(g, kind + "_high", [position, lever], signal=15, arguments=["1000"])
        stage(g, kind + "_passive", [position, lever])
    write_structure(OUTPUT / (g["id"] + ".nbt"), g["size"], blocks)
    guides.append(g)

    core = (6, 6, 6)
    g = guide("reactor", ["reactor_core", "reactor_stabilizer", "reactor_injector", "flux_gate"], 13, 13, core)
    blocks = base(13)
    blocks[core] = DE + "reactor_core"
    positions = []
    for dx, dz, facing in ((-4, 0, "east"), (4, 0, "west"), (0, -4, "south"), (0, 4, "north")):
        pos = (6 + dx, 6, 6 + dz)
        blocks[pos] = DE + "reactor_stabilizer"
        g["facings"].append({"pos": list(pos), "direction": facing})
        positions.append(pos)
    blocks[(6, 10, 6)] = DE + "reactor_injector"
    g["facings"].append({"pos": [6, 10, 6], "direction": "down"})
    blocks[(6, 11, 6)] = DE + "flux_gate[facing=down]"
    blocks[(1, 6, 6)] = DE + "flux_gate[facing=west]"
    stage(g, "reactor_marker", [core])
    stage(g, "reactor_stabilizers", positions)
    stage(g, "reactor_injector", [(6, 10, 6)])
    stage(g, "reactor_input", [(6, 11, 6)])
    stage(g, "reactor_output", [(1, 6, 6)])
    stage(g, "reactor_use", positions, action="reactor_fuel")
    write_structure(OUTPUT / (g["id"] + ".nbt"), g["size"], blocks)
    guides.append(g)
    (OUTPUT / "tutorials.json").write_text(json.dumps(guides, separators=(",", ":")) + "\n")
    (OUTPUT / "groups.json").write_text(json.dumps(
        [{"id": group, "icon": DE + icon} for group, icon in GROUP_ICONS.items()], indent=2) + "\n")
    for obsolete in ("fluid_gate", "flux_gate"):
        (OUTPUT / (obsolete + ".nbt")).unlink(missing_ok=True)
    print(f"Generated {len(guides)} Ponder structures and geometry catalog")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("de_jar", type=Path)
    args = parser.parse_args()
    if hashlib.sha256(args.de_jar.read_bytes()).hexdigest() != DE_SHA256:
        parser.error("The input must be the pinned Draconic Evolution artifact 8797042")
    with zipfile.ZipFile(args.de_jar) as jar:
        generate(jar)
