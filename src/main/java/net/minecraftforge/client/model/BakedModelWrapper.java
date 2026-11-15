package net.minecraftforge.client.model;

import java.util.List;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ItemTransforms;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.data.ModelData;

/**
 * Fabric port of Forge's wrapper. The five-argument getQuads()/getRenderTypes()
 * are ordinary methods on this class (not vanilla interface methods), so the
 * original annotations keep compiling. The three-argument vanilla
 * entry point forwards into them with ModelData.EMPTY.
 */
public class BakedModelWrapper<T extends BakedModel> implements ForgeBakedModel {
  protected final T originalModel;

  public BakedModelWrapper(T originalModel) { this.originalModel = originalModel; }

  public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource random) {
    return getQuads(state, side, random, ModelData.EMPTY, null);
  }
  public boolean useAmbientOcclusion() { return originalModel.useAmbientOcclusion(); }
  public boolean isGui3d() { return originalModel.isGui3d(); }
  public boolean usesBlockLight() { return originalModel.usesBlockLight(); }
  public boolean isCustomRenderer() { return originalModel.isCustomRenderer(); }
  public TextureAtlasSprite getParticleIcon() { return originalModel.getParticleIcon(); }
  public ItemTransforms getTransforms() { return originalModel.getTransforms(); }
  public ItemOverrides getOverrides() { return originalModel.getOverrides(); }

  public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData input) {
    return ForgeBakedModel.super.getModelData(level, pos, state, input);
  }
  public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource random,
                                            ModelData data, RenderType layer) {
    return originalModel.getQuads(state, side, random);
  }
  public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource random, ModelData data) {
    return ChunkRenderTypeSet.of(RenderType.solid());
  }
  public boolean useAmbientOcclusion(BlockState state) { return useAmbientOcclusion(); }
  public boolean useAmbientOcclusion(BlockState state, RenderType layer) { return useAmbientOcclusion(); }
}
