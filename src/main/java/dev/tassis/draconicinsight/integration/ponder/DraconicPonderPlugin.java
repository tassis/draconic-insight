package dev.tassis.draconicinsight.integration.ponder;

import com.brandon3055.draconicevolution.blocks.reactor.tileentity.TileReactorComponent;
import com.brandon3055.draconicevolution.blocks.energynet.tileentity.TileCrystalBase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.tassis.draconicinsight.DraconicInsight;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.catnip.levelWrappers.WrappedClientLevel;
import net.createmod.ponder.api.level.PonderLevel;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.createmod.ponder.api.registration.SharedTextRegistrationHelper;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.core.BlockPos;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

/** Loaded only by the guarded client setup when Ponder is installed. */
public final class DraconicPonderPlugin implements PonderPlugin {
    private static final String ROOT = "/assets/draconic_insight/ponder/";
    private static final String LANG_PREFIX = DraconicInsight.MOD_ID + ".ponder.";
    private final List<Tutorial> tutorials = read(ROOT + "tutorials.json", new TypeToken<List<Tutorial>>() {});
    private final List<Group> groups = read(ROOT + "groups.json", new TypeToken<List<Group>>() {});
    private final Map<String, String> english = read("/assets/draconic_insight/lang/en_us.json",
        new TypeToken<Map<String, String>>() {});

    public static void register() {
        PonderIndex.addPlugin(new DraconicPonderPlugin());
    }

    @Override
    public String getModId() {
        return DraconicInsight.MOD_ID;
    }

    @Override
    public void onPonderLevelRestore(PonderLevel level) {
        // DE's crystal tickers cast their level to ClientLevel when creating FX.
        // Keep their reads/writes in Ponder through its supported client facade.
        ClientLevel client = null;
        for (var entity : level.getBlockEntities()) {
            if (entity instanceof TileCrystalBase) {
                if (client == null) client = WrappedClientLevel.of(level);
                entity.setLevel(client);
            }
        }
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        for (Tutorial tutorial : tutorials) {
            helper.forComponents(tutorial.items.stream().map(ResourceLocation::parse).toList())
                .addStoryBoard(tutorial.id, (scene, util) -> build(scene, util, tutorial), groupId(tutorial.group));
        }
    }

    @Override
    public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
        for (Group group : groups) {
            helper.registerTag(groupId(group.id))
                .title(englishText("tag." + group.id))
                .description(englishText("tag." + group.id + ".description"))
                .item(BuiltInRegistries.ITEM.get(ResourceLocation.parse(group.icon)))
                .addToIndex()
                .register();
        }
        for (Tutorial tutorial : tutorials) {
            for (String item : tutorial.items) {
                helper.addTagToComponent(ResourceLocation.parse(item), groupId(tutorial.group));
            }
        }
    }

    @Override
    public void registerSharedText(SharedTextRegistrationHelper helper) {
        tutorials.stream().flatMap(tutorial -> tutorial.stages.stream()).map(Stage::caption).distinct()
            .forEach(key -> helper.registerSharedText(key, englishText("shared." + key)));
    }

    private void build(SceneBuilder scene, SceneBuildingUtil util, Tutorial tutorial) {
        scene.title(tutorial.id, englishText(tutorial.id + ".header"));
        scene.configureBasePlate(0, 0, tutorial.size.getFirst());
        float width = tutorial.size.getFirst();
        float height = tutorial.size.get(1);
        scene.scaleSceneView(Math.min(0.8f, 5.3f / (0.58f * width + 0.82f * height)));
        scene.setSceneOffsetY(1 - height / 2);
        scene.showBasePlate();
        if (tutorial.initial != null) {
            Selection context = util.select().position(pos(tutorial.initial.getFirst()));
            for (int i = 1; i < tutorial.initial.size(); i++) {
                context = context.add(util.select().position(pos(tutorial.initial.get(i))));
            }
            scene.world().showSection(context, Direction.DOWN);
        }
        if (tutorial.devices != null) {
            tutorial.devices.forEach(device -> DeviceSceneActions.initialize(scene, device));
        }
        for (Facing facing : tutorial.facings) {
            scene.world().modifyBlockEntity(pos(facing.pos), TileReactorComponent.class,
                component -> component.facing.set(Direction.valueOf(facing.direction.toUpperCase(java.util.Locale.ROOT))));
        }
        scene.idle(15);
        for (Stage stage : tutorial.stages) {
            Selection selection = util.select().position(pos(stage.positions.getFirst()));
            for (int i = 1; i < stage.positions.size(); i++) {
                selection = selection.add(util.select().position(pos(stage.positions.get(i))));
            }
            scene.world().showSection(selection, Direction.DOWN);
            if (tutorial.id.equals("reactor") && stage.caption.equals("reactor_marker")) {
                scene.addInstruction(current -> current.addElement(new ReactorCoreElement(pos(tutorial.core))));
            }
            DeviceSceneActions.apply(scene, util, pos(tutorial.core), stage);
            scene.overlay().showOutline(PonderPalette.WHITE, stage, selection, 100);
            var caption = scene.overlay().showText(100);
            if (stage.arguments == null) caption.sharedText(stage.caption);
            else caption.sharedText(stage.caption, stage.arguments.toArray());
            caption.independent(60).attachKeyFrame();
            scene.idle(110);
        }
        scene.rotateCameraY(90);
        scene.idle(40);
        scene.markAsFinished();
    }

    private static BlockPos pos(List<Integer> xyz) {
        return new BlockPos(xyz.get(0), xyz.get(1), xyz.get(2));
    }

    private static ResourceLocation groupId(String name) {
        return ResourceLocation.fromNamespaceAndPath(DraconicInsight.MOD_ID, name);
    }

    private String englishText(String suffix) {
        String key = LANG_PREFIX + suffix;
        String value = english.get(key);
        if (value == null || value.isBlank()) throw new IllegalStateException("Missing Ponder language entry: " + key);
        return value;
    }

    private static <T> T read(String name, TypeToken<T> type) {
        try (var input = DraconicPonderPlugin.class.getResourceAsStream(name)) {
            if (input == null) throw new IllegalStateException("Missing Ponder resource: " + name);
            return new Gson().fromJson(new InputStreamReader(input, StandardCharsets.UTF_8), type);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read Ponder resource: " + name, e);
        }
    }

    private record Tutorial(String id, String group, List<String> items, List<Integer> size, List<Integer> core,
                            List<Stage> stages, List<Facing> facings, List<Device> devices,
                            List<List<Integer>> initial) {
    }

    private record Group(String id, String icon) {
    }

    record Stage(String caption, List<List<Integer>> positions, String action, String item,
                 boolean sneak, String direction, Integer signal, List<String> arguments) {
    }

    record Device(String kind, List<Integer> pos, String direction, String item, Long minimum, Long maximum) {
    }

    private record Facing(List<Integer> pos, String direction) {
    }
}
