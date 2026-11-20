package com.sora.splineroads.fabric.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelDataContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Equivalent of Forge's patched BlockRenderDispatcher.renderBatched, which
 * computes BakedModel.getModelData(level, pos, state, EMPTY) before meshing.
 * The mixin is deliberately descriptor-free (renderBatched is unique in 1.20.1)
 * and only handles a thread-local context, so it is safe if other renderers
 * are installed.
 */
@Mixin(BlockRenderDispatcher.class)
public abstract class BlockRenderDispatcherMixin {
  @Inject(method = "renderBatched", at = @At("HEAD"))
  private void sr$begin(BlockState state, BlockPos pos, BlockAndTintGetter level,
                        PoseStack pose, VertexConsumer consumer, boolean checkSides,
                        RandomSource random, CallbackInfo info) {
    BakedModel model = ((BlockRenderDispatcher) (Object) this).getBlockModel(state);
    ModelDataContext.begin(level, pos, state, model);
  }

  @Inject(method = "renderBatched", at = @At("RETURN"))
  private void sr$end(BlockState state, BlockPos pos, BlockAndTintGetter level,
                      PoseStack pose, VertexConsumer consumer, boolean checkSides,
                      RandomSource random, CallbackInfo info) { ModelDataContext.end(); }
}
