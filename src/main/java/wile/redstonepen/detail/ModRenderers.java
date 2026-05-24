/*
 * @file ModRenderers.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 */
package wile.redstonepen.detail;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import wile.redstonepen.blocks.RedstoneTrack;

public class ModRenderers
{
  @Environment(EnvType.CLIENT)
  public static class TrackTer implements BlockEntityRenderer<RedstoneTrack.TrackBlockEntity, BlockEntityRenderState>
  {
    public static void registerModels()
    {}

    public TrackTer(BlockEntityRendererProvider.Context renderer)
    {}

    @Override
    public BlockEntityRenderState createRenderState()
    { return new BlockEntityRenderState(); }

    @Override
    public void extractRenderState(RedstoneTrack.TrackBlockEntity te, BlockEntityRenderState state, float partialTick, Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay)
    { BlockEntityRenderState.extractBase(te, state, crumblingOverlay); }

    @Override
    public void submit(BlockEntityRenderState state, PoseStack mxs, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState)
    {}
  }
}
