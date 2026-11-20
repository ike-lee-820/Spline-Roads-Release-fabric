package com.sora.splineroads.fabric.mixin;

import com.sora.splineroads.client.RoadChunkUploads;
import com.sora.splineroads.client.RoadTerrainHandoff;
import java.util.Queue;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Replaces the Forge coremod's upload-queue accessor on ChunkRenderDispatcher. */
@Mixin(ChunkRenderDispatcher.class)
public abstract class ChunkRenderDispatcherMixin implements RoadChunkUploads {
  @Shadow @Final private Queue<Runnable> toUpload;

  @Override public Queue<Runnable> splineroads$uploads() { return toUpload; }

  @Inject(method = "uploadAllPendingUploads", at = @At("HEAD"))
  private void sr$beginUploads(CallbackInfo info) {
    RoadTerrainHandoff.beginUploads((ChunkRenderDispatcher) (Object) this);
  }

  @Inject(method = "dispose", at = @At("TAIL"))
  private void sr$discardUploads(CallbackInfo info) {
    RoadTerrainHandoff.discardUploads((ChunkRenderDispatcher) (Object) this);
  }
}
