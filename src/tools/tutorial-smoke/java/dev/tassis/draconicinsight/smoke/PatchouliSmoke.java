package dev.tassis.draconicinsight.smoke;

import com.brandon3055.draconicevolution.blocks.reactor.tileentity.TileReactorComponent;
import com.brandon3055.draconicevolution.blocks.tileentity.TileEnergyCoreStabilizer;
import com.brandon3055.draconicevolution.blocks.tileentity.TileEnergyPylon;
import com.brandon3055.draconicevolution.blocks.tileentity.TileStructureBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Rotation;
import vazkii.patchouli.common.multiblock.MultiblockRegistry;
import vazkii.patchouli.common.multiblock.AbstractMultiblock;

final class PatchouliSmoke {
    static int verifyFormedGuides() {
        var server = Minecraft.getInstance().getSingleplayerServer();
        var result = new java.util.concurrent.CompletableFuture<Integer>();
        server.execute(() -> {
            try {
                var world = server.overworld();
                int count = 0;
                for (String id : new String[] {"energy_pylon", "advanced_stabilizers"}) {
                    var diagram = (AbstractMultiblock) MultiblockRegistry.MULTIBLOCKS.get(
                        ResourceLocation.fromNamespaceAndPath("draconic_insight", id));
                    BlockPos anchor = new BlockPos(32 + count * 16, 100, 32);
                    diagram.place(world, anchor, Rotation.NONE);
                    if (!diagram.validate(world, anchor, Rotation.NONE)) throw new AssertionError("Raw guide rejected: " + id);
                    var controller = world.getBlockEntity(anchor);
                    if (controller instanceof TileEnergyCoreStabilizer stabilizer) stabilizer.onPlaced();
                    if (controller instanceof TileEnergyPylon pylon) pylon.validateStructure();
                    int parts = 0;
                    TileStructureBlock sample = null;
                    for (BlockPos p : BlockPos.betweenClosed(anchor.offset(-2, -2, -2), anchor.offset(2, 2, 2))) {
                        if (world.getBlockEntity(p) instanceof TileStructureBlock part) {
                            sample = part;
                            parts++;
                        }
                    }
                    if (parts != (id.equals("energy_pylon") ? 1 : 8)) throw new AssertionError("DE structure did not form: " + id);
                    if (!diagram.validate(world, anchor, Rotation.NONE)) throw new AssertionError("Formed guide rejected: " + id);
                    sample.blockName.set(ResourceLocation.parse("minecraft:dirt"));
                    if (diagram.validate(world, anchor, Rotation.NONE)) throw new AssertionError("Wrong material accepted: " + id);
                    count++;
                }
                result.complete(count);
            } catch (Throwable error) {
                result.completeExceptionally(error);
            }
        });
        return result.join();
    }

    static int verify() {
        int count = 0;
        for (String name : new String[] {"reactor_stabilizers", "reactor_injector"}) {
            var diagram = (AbstractMultiblock) MultiblockRegistry.MULTIBLOCKS.get(ResourceLocation.fromNamespaceAndPath("draconic_insight", name));
            if (diagram == null) throw new AssertionError("Unregistered reactor diagram: " + name);
            BlockPos core = name.equals("reactor_injector") ? BlockPos.ZERO : new BlockPos(4, 0, 4);
            int components = 0;
            for (BlockPos p : BlockPos.betweenClosed(BlockPos.ZERO, new BlockPos(diagram.getSize()).offset(-1, -1, -1))) {
                if (diagram.getBlockEntity(p) instanceof TileReactorComponent component) {
                    BlockPos delta = core.subtract(p);
                    Direction expected = Direction.getNearest(delta.getX(), delta.getY(), delta.getZ());
                    if (component.facing.get() != expected) throw new AssertionError("Wrong reactor facing at " + p);
                    components++;
                }
            }
            if (components != (name.equals("reactor_injector") ? 1 : 4)) throw new AssertionError("Missing reactor components");
            count++;
        }
        return count;
    }
}
