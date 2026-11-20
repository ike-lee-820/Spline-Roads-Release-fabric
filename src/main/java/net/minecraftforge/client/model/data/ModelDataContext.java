package net.minecraftforge.client.model.data;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.ForgeBakedModel;
import net.minecraftforge.client.model.ForgeBakedModels;

/**
 * Fabric port of Forge's per-position BakedModel.getModelData() call.
 * Vanilla's three-argument getQuads() has no level/pos, so the chunk rebuild
 * mixin publishes the computed ModelData here for the duration of one block.
 * When the mixin is absent the context is simply EMPTY, which preserves the
 * original vanilla behaviour instead of failing.
 */
public final class ModelDataContext {
  private static final ThreadLocal<ModelData> CURRENT =
      ThreadLocal.withInitial(() -> ModelData.EMPTY);

  public static ModelData current() {
    ModelData value = CURRENT.get();
    return value == null ? ModelData.EMPTY : value;
  }

  public static void begin(BlockAndTintGetter level, BlockPos pos, BlockState state, BakedModel model) {
    if (level == null || pos == null || model == null) { end(); return; }
    try {
      ForgeBakedModel extended = model instanceof ForgeBakedModel forge
          ? forge : ForgeBakedModels.of(model);
      ModelData data = extended.getModelData(level, pos, state, ModelData.EMPTY);
      CURRENT.set(data == null ? ModelData.EMPTY : data);
    } catch (Throwable failure) {
      // A bad per-cell snapshot must never break chunk meshing.
      CURRENT.set(ModelData.EMPTY);
    }
  }

  public static void end() { CURRENT.set(ModelData.EMPTY); }
  private ModelDataContext() {}
}
