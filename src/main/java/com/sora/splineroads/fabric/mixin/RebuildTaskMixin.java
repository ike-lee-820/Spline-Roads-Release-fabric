package com.sora.splineroads.fabric.mixin;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.sora.splineroads.client.RoadTerrainHandoff;
import com.sora.splineroads.fabric.FabricApiBridge;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fabric equivalent of Forge's terrain_upload.js vanilla transformer.
 * Targets are the verified Fabric Loom / intermediary names (require = 1), so
 * a mapping change fails loudly instead of silently dropping the upload
 * receipts. Field lookups are by type so mojmap / intermediary field names do
 * not matter.
 */
@Mixin(targets = "net.minecraft.client.renderer.chunk.ChunkRenderDispatcher$RenderChunk$RebuildTask")
public abstract class RebuildTaskMixin {
  private static final String DO_TASK =
      "doTask(Lnet/minecraft/client/renderer/ChunkBufferBuilderPack;)Ljava/util/concurrent/CompletableFuture;";
  // Fabric Loom / intermediary names for the two synthetic lambdas in
  // ChunkRenderDispatcher$RenderChunk$RebuildTask (verified via javap on 1.20.1).
  private static final String UPLOAD_LAMBDA =
      "method_22788(Ljava/util/List;Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$CompiledChunk;Lnet/minecraft/client/renderer/RenderType;Lcom/mojang/blaze3d/vertex/BufferBuilder$RenderedBuffer;)V";
  private static final String FINISH_LAMBDA =
      "method_23619(Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$CompiledChunk;Ljava/util/List;Ljava/lang/Throwable;)Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$ChunkTaskResult;";
  private static final ThreadLocal<RenderType> LAYER = new ThreadLocal<>();

  @Inject(method = DO_TASK, at = @At("HEAD"), require = 1)
  private void sr$begin(ChunkBufferBuilderPack pack, CallbackInfo info) {
    try {
      Object chunk = FabricApiBridge.field(this, ChunkRenderDispatcher.RenderChunk.class);
      AtomicBoolean cancelled = (AtomicBoolean) FabricApiBridge.field(this, AtomicBoolean.class);
      ChunkRenderDispatcher dispatcher = chunk == null ? null
          : (ChunkRenderDispatcher) FabricApiBridge.field(chunk, ChunkRenderDispatcher.class);
      if (chunk != null && cancelled != null && dispatcher != null)
        RoadTerrainHandoff.beginVanilla(this, (ChunkRenderDispatcher.RenderChunk) chunk, cancelled, dispatcher);
    } catch (Throwable ignored) { }
  }

  @Inject(method = DO_TASK, at = @At("RETURN"), require = 1)
  @SuppressWarnings("rawtypes")
  private void sr$end(ChunkBufferBuilderPack pack, CallbackInfoReturnable<CompletableFuture> info) {
    try { RoadTerrainHandoff.endVanilla(info.getReturnValue(), this); }
    catch (Throwable ignored) { }
  }

  @Redirect(method = DO_TASK, require = 1,
      at = @At(value = "INVOKE",
          target = "Ljava/util/concurrent/CompletableFuture;handle(Ljava/util/function/BiFunction;)Ljava/util/concurrent/CompletableFuture;"))
  @SuppressWarnings({"rawtypes", "unchecked"})
  private CompletableFuture sr$complete(CompletableFuture future, BiFunction handler) {
    try { return RoadTerrainHandoff.completeVanilla(future, handler, this); }
    catch (Throwable ignored) { return future.handle(handler); }
  }

  @Inject(method = UPLOAD_LAMBDA, at = @At("HEAD"), require = 1)
  private void sr$layer(List<?> list, ChunkRenderDispatcher.CompiledChunk compiled,
                        RenderType layer, BufferBuilder.RenderedBuffer buffer, CallbackInfo info) {
    LAYER.set(layer);
  }

  @Redirect(method = UPLOAD_LAMBDA, require = 1,
      at = @At(value = "INVOKE",
          target = "Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher;uploadChunkLayer(Lcom/mojang/blaze3d/vertex/BufferBuilder$RenderedBuffer;Lcom/mojang/blaze3d/vertex/VertexBuffer;)Ljava/util/concurrent/CompletableFuture;"))
  private CompletableFuture<Void> sr$upload(ChunkRenderDispatcher dispatcher,
                                            BufferBuilder.RenderedBuffer data, VertexBuffer live) {
    RenderType layer = LAYER.get();
    if (layer == null) return dispatcher.uploadChunkLayer(data, live);
    try { return RoadTerrainHandoff.uploadVanilla(dispatcher, data, live, this, layer); }
    catch (Throwable ignored) { return dispatcher.uploadChunkLayer(data, live); }
  }

  @Inject(method = FINISH_LAMBDA, at = @At("HEAD"), require = 1)
  private void sr$beforeCommit(ChunkRenderDispatcher.CompiledChunk compiled, List<?> list,
                               Throwable failure, CallbackInfo info) {
    try { RoadTerrainHandoff.beforeCommit(this, failure); }
    catch (Throwable ignored) { }
  }

  @Redirect(method = FINISH_LAMBDA, require = 1,
      at = @At(value = "INVOKE",
          target = "Ljava/util/concurrent/atomic/AtomicReference;set(Ljava/lang/Object;)V"))
  private void sr$commit(AtomicReference<Object> reference, Object compiled) {
    try { RoadTerrainHandoff.commitVanilla(reference, compiled, this); }
    catch (Throwable ignored) { reference.set(compiled); }
  }
}
