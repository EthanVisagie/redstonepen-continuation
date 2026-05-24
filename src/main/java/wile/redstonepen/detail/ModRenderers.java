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
import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.SimpleUnbakedExtraModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import wile.redstonepen.ModConstants;
import wile.redstonepen.blocks.RedstoneTrack;
import wile.redstonepen.blocks.RedstoneTrack.defs.connections;

import java.util.ArrayList;

public class ModRenderers
{
  @Environment(EnvType.CLIENT)
  public static class TrackTer implements BlockEntityRenderer<RedstoneTrack.TrackBlockEntity, TrackTer.TrackRenderState>
  {
    @SuppressWarnings("unchecked")
    private static final ExtraModelKey<BlockStateModel>[] model_keys = new ExtraModelKey[RedstoneTrack.defs.STATE_FLAG_WIR_COUNT];
    @SuppressWarnings("unchecked")
    private static final ExtraModelKey<BlockStateModel>[] modelm_keys = new ExtraModelKey[RedstoneTrack.defs.STATE_FLAG_CON_COUNT];
    @SuppressWarnings("unchecked")
    private static final ExtraModelKey<BlockStateModel>[] modelc_keys = new ExtraModelKey[RedstoneTrack.defs.STATE_FLAG_CON_COUNT];
    private static final ArrayList<Vec3> power_rgb = new ArrayList<>();

    public static void registerModels(ModelLoadingPlugin.Context context)
    {
      RedstoneTrack.defs.models.STATE_WIRE_MAPPING.forEach((flags, name)->{
        final Identifier model = getModelResourceLocation(name);
        final ExtraModelKey<BlockStateModel> key = createModelKey(model, context);
        for(int i=0; i<RedstoneTrack.defs.STATE_FLAG_WIR_COUNT; ++i) {
          if((flags & (1L << (RedstoneTrack.defs.STATE_FLAG_WIR_POS+i))) != 0) {
            model_keys[i] = key;
            break;
          }
        }
      });
      RedstoneTrack.defs.models.STATE_CONNECT_MAPPING.forEach((flags, name)->{
        final Identifier model = getModelResourceLocation(name);
        final ExtraModelKey<BlockStateModel> key = createModelKey(model, context);
        for(int i=0; i<RedstoneTrack.defs.STATE_FLAG_CON_COUNT; ++i) {
          if((flags & (1L << (RedstoneTrack.defs.STATE_FLAG_CON_POS+i))) != 0) {
            modelc_keys[i] = key;
            break;
          }
        }
      });
      RedstoneTrack.defs.models.STATE_CNTWIRE_MAPPING.forEach((flags, name)->{
        final Identifier model = getModelResourceLocation(name);
        final ExtraModelKey<BlockStateModel> key = createModelKey(model, context);
        for(int i=0; i<RedstoneTrack.defs.STATE_FLAG_CON_COUNT; ++i) {
          if((flags & (1L << (RedstoneTrack.defs.STATE_FLAG_CON_POS+i))) != 0) {
            modelm_keys[i] = key;
            break;
          }
        }
      });
      power_rgb.clear();
      for(int i = 0; i <= 15; ++i) {
        float f = (float)i / 15.0f;
        power_rgb.add(new Vec3(
          Mth.clamp(0.01f + f, 0.0F, 1f),
          Mth.clamp(0.01f + f * 0.4f-.3f, 0.0F, 1f),
          Mth.clamp(0.01f + f * 0.4f-.2f, 0.0F, 1f)
        ));
      }
    }

    private static ExtraModelKey<BlockStateModel> createModelKey(Identifier model, ModelLoadingPlugin.Context context)
    {
      final ExtraModelKey<BlockStateModel> key = ExtraModelKey.create(model::toString);
      context.addModel(key, SimpleUnbakedExtraModel.blockStateModel(model));
      return key;
    }

    private static Identifier getModelResourceLocation(String name)
    { return Identifier.fromNamespaceAndPath(ModConstants.MODID, name).withPrefix("item/"); }

    private static Vec3 getPowerRGB(int p)
    { return power_rgb.isEmpty() ? Vec3.ZERO : power_rgb.get(p & 0xf); }

    public TrackTer(BlockEntityRendererProvider.Context renderer)
    {}

    @Override
    public TrackRenderState createRenderState()
    { return new TrackRenderState(); }

    @Override
    public void extractRenderState(RedstoneTrack.TrackBlockEntity te, TrackRenderState state, float partialTick, Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay)
    {
      BlockEntityRenderState.extractBase(te, state, crumblingOverlay);
      state.wireFlags = te.getWireFlags();
      state.wireFlagCount = te.getWireFlagCount();
      state.connectionFlags = te.getConnectionFlags();
      state.connectionFlagCount = te.getConnectionFlagCount();
      for(int i=0; i<state.sidePower.length; ++i) state.sidePower[i] = te.getSidePower(connections.CONNECTION_BIT_ORDER[i]);
    }

    @Override
    public void submit(TrackRenderState state, PoseStack mxs, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState)
    {
      final var renderType = ItemBlockRenderTypes.getRenderType(state.blockState);
      long wire = 0x1;
      for(int i=0; i<state.wireFlagCount; ++i, wire<<=1) {
        if((state.wireFlags & wire) == 0) continue;
        submitModel(state, mxs, submitNodeCollector, renderType, model_keys[i], state.sidePower[i/4]);
      }
      long wireMask = 0xfL;
      long connection = 0x1L;
      for(int i=0; i<state.connectionFlagCount; ++i, connection<<=1, wireMask<<=4) {
        if(((state.wireFlags & wireMask)==0) && ((state.connectionFlags & connection)==0)) continue;
        submitModel(state, mxs, submitNodeCollector, renderType, ((state.connectionFlags & connection)==0) ? modelm_keys[i] : modelc_keys[i], state.sidePower[i]);
      }
    }

    private static void submitModel(TrackRenderState state, PoseStack mxs, SubmitNodeCollector submitNodeCollector, net.minecraft.client.renderer.rendertype.RenderType renderType, ExtraModelKey<BlockStateModel> key, int power)
    {
      if(key == null) return;
      final BlockStateModel model = Minecraft.getInstance().getModelManager().getModel(key);
      final Vec3 rgb = getPowerRGB(power);
      submitNodeCollector.submitBlockModel(mxs, renderType, model, (float)rgb.x(), (float)rgb.y(), (float)rgb.z(), state.lightCoords, OverlayTexture.NO_OVERLAY, -1);
    }

    public static class TrackRenderState extends BlockEntityRenderState
    {
      public int wireFlags;
      public int wireFlagCount;
      public int connectionFlags;
      public int connectionFlagCount;
      public final int[] sidePower = new int[RedstoneTrack.defs.STATE_FLAG_CON_COUNT];
    }
  }
}
