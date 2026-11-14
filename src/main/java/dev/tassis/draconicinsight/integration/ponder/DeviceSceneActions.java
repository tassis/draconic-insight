package dev.tassis.draconicinsight.integration.ponder;

import com.brandon3055.draconicevolution.blocks.energynet.tileentity.TileCrystalDirectIO;
import com.brandon3055.draconicevolution.blocks.energynet.tileentity.TileCrystalWirelessIO;
import com.brandon3055.draconicevolution.blocks.reactor.tileentity.TileReactorCore;
import com.brandon3055.draconicevolution.blocks.tileentity.TileEnergyTransfuser;
import com.brandon3055.draconicevolution.blocks.tileentity.TileStabilizedSpawner;
import com.brandon3055.draconicevolution.blocks.tileentity.flowgate.TileFlowGate;
import com.brandon3055.draconicevolution.init.DEContent;
import java.util.List;
import java.util.Locale;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

/** Scene-local preview instructions, with no real-world edits or player interaction packets. */
final class DeviceSceneActions {
    private DeviceSceneActions() {
    }

    static void initialize(SceneBuilder scene, DraconicPonderPlugin.Device device) {
        BlockPos position = pos(device.pos());
        switch (device.kind()) {
            case "io_input", "io_output" -> scene.world().modifyBlockEntity(position, TileCrystalDirectIO.class, tile -> {
                tile.outputMode.set(device.kind().equals("io_output"));
                tile.updateRotation(direction(device.direction()));
            });
            case "transfuser" -> scene.world().modifyBlockEntity(position, TileEnergyTransfuser.class,
                tile -> tile.itemNorth.setStackInSlot(0, stack(device.item())));
            case "flow_gate" -> scene.world().modifyBlockEntity(position, TileFlowGate.class, tile -> {
                tile.minFlow.set(device.minimum());
                tile.maxFlow.set(device.maximum());
                tile.rsSignal.set(0);
            });
            case "vanilla_spawner" -> scene.world().modifyBlockEntity(position, SpawnerBlockEntity.class,
                tile -> tile.setEntityId(EntityType.PIG, tile.getLevel().random));
            default -> throw new IllegalArgumentException("Unknown preview device: " + device.kind());
        }
    }

    static void apply(SceneBuilder scene, SceneBuildingUtil util, BlockPos core, DraconicPonderPlugin.Stage stage) {
        List<BlockPos> positions = stage.positions().stream().map(DeviceSceneActions::pos).toList();
        BlockPos subject = positions.getFirst();
        String action = stage.action();
        if (action != null) {
            switch (action) {
                case "network_link" -> arrow(scene, Vec3.atCenterOf(subject), Vec3.atCenterOf(positions.get(1)));
                case "network_supply" -> arrow(scene, Vec3.atCenterOf(positions.get(1)), Vec3.atCenterOf(subject));
                case "crystal_route" -> {
                    for (int i = 1; i < positions.size(); i++) {
                        arrow(scene, Vec3.atCenterOf(positions.get(i - 1)), Vec3.atCenterOf(positions.get(i)));
                    }
                }
                case "binding_clear" -> { }
                case "receiver_link" -> arrow(scene, Vec3.atCenterOf(positions.get(1)),
                    util.vector().blockSurface(subject, direction(stage.direction())));
                case "wireless_input" -> scene.world().modifyBlockEntity(subject, TileCrystalWirelessIO.class,
                    tile -> tile.inputMode.set(true));
                case "io_output" -> scene.world().modifyBlockEntity(subject, TileCrystalDirectIO.class, tile -> {
                    tile.outputMode.set(true);
                    tile.updateRotation(tile.facing.get());
                });
                case "io_delivery" -> arrow(scene, Vec3.atCenterOf(subject),
                    util.vector().blockSurface(positions.get(1), Direction.UP));
                case "spawner_convert" -> {
                    scene.world().setBlock(subject, Blocks.AIR.defaultBlockState(), true);
                    scene.world().createItemEntity(Vec3.atCenterOf(subject).add(0, 0.2, 0), Vec3.ZERO,
                        new ItemStack(DEContent.STABILIZED_SPAWNER.get()));
                }
                case "spawner_place" -> {
                    scene.world().modifyEntities(ItemEntity.class, Entity::discard);
                    scene.world().setBlock(subject, DEContent.STABILIZED_SPAWNER.get().defaultBlockState(), true);
                    scene.world().modifyBlockEntity(subject, TileStabilizedSpawner.class,
                        tile -> tile.mobSoul.set(soul("minecraft:pig")));
                }
                case "spawner_soul" -> scene.world().modifyBlockEntity(subject, TileStabilizedSpawner.class,
                    tile -> tile.mobSoul.set(soul("minecraft:cow")));
                case "spawner_upgrade" -> scene.world().modifyBlockEntity(subject, TileStabilizedSpawner.class,
                    tile -> tile.spawnerTier.set(TileStabilizedSpawner.SpawnerTier.WYVERN));
                case "spawner_stop" -> {
                    scene.world().modifyBlock(subject, state -> state.setValue(BlockStateProperties.POWERED, true), false);
                    scene.world().modifyBlockEntity(positions.get(1), TileStabilizedSpawner.class,
                        tile -> tile.isPowered.set(true));
                }
                case "gate_direction" -> {
                    for (BlockPos position : positions) {
                        Vec3 center = Vec3.atCenterOf(position);
                        arrow(scene, center.add(-2, 0, 0), center.add(-0.6, 0, 0));
                        arrow(scene, center.add(0.6, 0, 0), center.add(2, 0, 0));
                    }
                }
                case "gate_configure" -> { }
                case "reactor_fuel" -> scene.world().modifyBlockEntity(core, TileReactorCore.class,
                    tile -> tile.reactableFuel.set(1296));
                default -> throw new IllegalArgumentException("Unknown preview action: " + action);
            }
        }
        if (stage.signal() != null) {
            scene.world().modifyBlockEntity(subject, TileFlowGate.class, tile -> tile.rsSignal.set(stage.signal()));
            scene.world().modifyBlock(positions.get(1),
                state -> state.setValue(BlockStateProperties.POWERED, stage.signal() > 0), false);
        }
        boolean signalClick = stage.signal() != null && stage.signal() == 15;
        if (stage.item() != null || stage.sneak() || "gate_configure".equals(action)
            || "spawner_stop".equals(action) || signalClick) {
            BlockPos clicked = signalClick ? positions.get(1) : subject;
            Direction side = stage.direction() == null ? Direction.UP : direction(stage.direction());
            Vec3 controlPoint = "binding_clear".equals(action)
                ? Vec3.atCenterOf(clicked).add(0, 1.8, 0) : util.vector().blockSurface(clicked, side);
            var controls = scene.overlay().showControls(controlPoint, Pointing.DOWN, 70)
                .rightClick();
            if (stage.item() != null) controls.withItem(stage.item().endsWith(":mob_soul")
                ? soul("minecraft:cow") : stack(stage.item()));
            if (stage.sneak()) controls.whileSneaking();
        }
    }

    private static void arrow(SceneBuilder scene, Vec3 from, Vec3 to) {
        scene.overlay().showBigLine(PonderPalette.WHITE, from, to, 100);
        Vec3 along = to.subtract(from).normalize();
        Vec3 reference = Math.abs(along.y) > 0.9 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3 side = along.cross(reference).normalize().scale(0.18);
        Vec3 base = to.subtract(along.scale(0.35));
        scene.overlay().showLine(PonderPalette.WHITE, to, base.add(side), 100);
        scene.overlay().showLine(PonderPalette.WHITE, to, base.subtract(side), 100);
    }

    private static ItemStack soul(String entity) {
        ItemStack stack = new ItemStack(DEContent.MOB_SOUL.get());
        DEContent.MOB_SOUL.get().setEntity(ResourceLocation.parse(entity), stack);
        return stack;
    }

    private static ItemStack stack(String id) {
        ResourceLocation key = ResourceLocation.parse(id);
        if (!BuiltInRegistries.ITEM.containsKey(key)) throw new IllegalArgumentException("Unknown tutorial item: " + id);
        return new ItemStack(BuiltInRegistries.ITEM.get(key));
    }

    private static BlockPos pos(List<Integer> xyz) {
        return new BlockPos(xyz.get(0), xyz.get(1), xyz.get(2));
    }

    private static Direction direction(String name) {
        return Direction.valueOf(name.toUpperCase(Locale.ROOT));
    }
}
