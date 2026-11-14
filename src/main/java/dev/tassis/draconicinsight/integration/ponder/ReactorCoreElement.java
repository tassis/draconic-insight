package dev.tassis.draconicinsight.integration.ponder;

import com.brandon3055.brandonscore.client.render.BlockEntityRendererTransparent;
import com.brandon3055.draconicevolution.blocks.reactor.tileentity.TileReactorCore;
import net.createmod.ponder.api.element.PonderSceneElement;
import net.createmod.ponder.api.level.PonderLevel;
import net.createmod.ponder.foundation.element.PonderElementBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;

/** DE normally draws this core in its world-transparent pass, which Ponder does not run. */
final class ReactorCoreElement extends PonderElementBase implements PonderSceneElement {
    private final BlockPos position;

    ReactorCoreElement(BlockPos position) {
        this.position = position;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void renderFirst(PonderLevel world, MultiBufferSource buffers, GuiGraphics graphics, float partialTick) {
        if (!(world.getBlockEntity(position) instanceof TileReactorCore core)) return;
        var renderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(core);
        if (!(renderer instanceof BlockEntityRendererTransparent<?>)) return;
        var transparent = (BlockEntityRendererTransparent<TileReactorCore>) renderer;
        var pose = graphics.pose();
        pose.pushPose();
        try {
            pose.translate(position.getX(), position.getY(), position.getZ());
            transparent.renderTransparent(core, partialTick, pose, buffers, LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY);
        } finally {
            pose.popPose();
        }
    }

    @Override
    public void renderLayer(PonderLevel world, MultiBufferSource buffers, RenderType type, GuiGraphics graphics,
                            float partialTick) {
    }

    @Override
    public void renderLast(PonderLevel world, MultiBufferSource buffers, GuiGraphics graphics, float partialTick) {
    }
}
