package net.minecraftforge.client.model;

import java.lang.reflect.Method;
import java.util.List;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.data.ModelData;

/**
 * Fabric port: Forge patches BakedModel with these overloads. On Fabric that
 * patch does not exist, so the extra methods live on this sub-interface.
 * Vanilla's three-argument getQuads() still drives rendering; getModelData()
 * recovers per-block data from the BlockEntity (e.g. RoadFillEntity) by reflection.
 */
public interface ForgeBakedModel extends BakedModel {
  default ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData input) {
    ModelData result = input == null ? ModelData.EMPTY : input;
    if (level == null || pos == null) return result;
    try {
      BlockEntity entity = level.getBlockEntity(pos);
      if (entity == null) return result;
      Method accessor = entity.getClass().getMethod("getModelData");
      Object value = accessor.invoke(entity);
      if (value instanceof ModelData data) return ModelData.merge(result, data);
    } catch (Throwable ignored) {
      // Plain vanilla block entities have no getModelData().
    }
    return result;
  }

  default List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource random,
                                   ModelData data, RenderType layer) {
    return getQuads(state, side, random);
  }
  default ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource random, ModelData data) {
    return ChunkRenderTypeSet.of(RenderType.solid());
  }
  default boolean useAmbientOcclusion(BlockState state) { return useAmbientOcclusion(); }
  default boolean useAmbientOcclusion(BlockState state, RenderType layer) { return useAmbientOcclusion(); }
  default boolean isVanillaAdapter() { return true; }
}
