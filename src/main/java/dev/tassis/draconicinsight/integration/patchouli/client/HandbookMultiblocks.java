package dev.tassis.draconicinsight.integration.patchouli.client;

import com.brandon3055.draconicevolution.blocks.reactor.tileentity.TileReactorComponent;
import com.brandon3055.draconicevolution.blocks.tileentity.TileStructureBlock;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import dev.tassis.draconicinsight.DraconicInsight;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.BlockGetter;
import vazkii.patchouli.api.IStateMatcher;
import vazkii.patchouli.api.TriPredicate;
import vazkii.patchouli.api.PatchouliAPI;
import vazkii.patchouli.common.multiblock.DenseMultiblock;
import vazkii.patchouli.common.multiblock.StringStateMatcher;

/** Preview BE orientation and recognition of DE's formed structure placeholders. */
public final class HandbookMultiblocks {
    private HandbookMultiblocks() {
    }

    public static void register() {
        for (String entry : List.of("reactor/setup", "energy_storage/stabilizers", "energy_storage/energy_pylon")) {
            registerEntry(entry);
        }
    }

    private static void registerEntry(String entryPath) {
        String path = "/assets/draconic_insight/patchouli_books/draconic_insight/en_us/entries/" + entryPath + ".json";
        try (var input = HandbookMultiblocks.class.getResourceAsStream(path)) {
            if (input == null) throw new IllegalStateException("Missing handbook diagrams: " + entryPath);
            var pages = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8))
                .getAsJsonObject().getAsJsonArray("pages");
            for (var element : pages) {
                var page = element.getAsJsonObject();
                if (!page.has("multiblock_id")) continue;
                var definition = page.getAsJsonObject("multiblock");
                String[][] pattern = new Gson().fromJson(definition.get("pattern"), String[][].class);
                List<Object> targets = new ArrayList<>();
                definition.getAsJsonObject("mapping").entrySet().forEach(entry -> {
                    targets.add(entry.getKey().charAt(0));
                    targets.add(formedMatcher(entry.getValue().getAsString()));
                });
                var multiblock = new HandbookDiagram(pattern, targets.toArray());
                var offset = definition.getAsJsonArray("offset");
                multiblock.offset(offset.get(0).getAsInt(), offset.get(1).getAsInt(), offset.get(2).getAsInt());
                multiblock.setSymmetrical(definition.has("symmetrical") && definition.get("symmetrical").getAsBoolean());
                PatchouliAPI.get().registerMultiblock(ResourceLocation.parse(page.get("multiblock_id").getAsString()),
                    multiblock);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to register " + DraconicInsight.MOD_ID + " diagrams", e);
        }
    }

    private static IStateMatcher formedMatcher(String state) {
        try {
            IStateMatcher original = StringStateMatcher.fromString(state);
            return new IStateMatcher() {
                @Override
                public BlockState getDisplayedState(long ticks) {
                    return original.getDisplayedState(ticks);
                }

                @Override
                public TriPredicate<BlockGetter, BlockPos, BlockState> getStatePredicate() {
                    return (world, pos, actual) -> original.getStatePredicate().test(world, pos, actual)
                        || (world.getBlockEntity(pos) instanceof TileStructureBlock part
                            && part.getController() != null
                            && original.getStatePredicate().test(world, pos, part.getOriginalBlock().defaultBlockState()));
                }
            };
        } catch (CommandSyntaxException e) {
            throw new IllegalArgumentException("Invalid handbook block state: " + state, e);
        }
    }

    private static final class HandbookDiagram extends DenseMultiblock {
        private final BlockPos core;

        private HandbookDiagram(String[][] pattern, Object... targets) {
            super(pattern, targets);
            core = findCore(pattern);
        }

        @Override
        public BlockEntity getBlockEntity(BlockPos pos) {
            BlockEntity entity = super.getBlockEntity(pos);
            if (entity instanceof TileReactorComponent component && core != null) {
                BlockPos delta = core.subtract(pos);
                component.facing.set(Direction.getNearest(delta.getX(), delta.getY(), delta.getZ()));
            }
            return entity;
        }

        private static BlockPos findCore(String[][] pattern) {
            for (int y = 0; y < pattern.length; y++) {
                for (int x = 0; x < pattern[y].length; x++) {
                    int z = pattern[y][x].indexOf('C');
                    if (z >= 0) return new BlockPos(x, pattern.length - y - 1, z);
                }
            }
            return null;
        }
    }
}
