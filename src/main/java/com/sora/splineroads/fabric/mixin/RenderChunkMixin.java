package com.sora.splineroads.fabric.mixin;

import com.sora.splineroads.client.RoadChunkBuffers;
import com.sora.splineroads.client.RoadTerrainHandoff;
import java.util.Map;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import com.mojang.blaze3d.vertex.VertexBuffer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Replaces the Forge coremod's interface injection on RenderChunk. */
@Mixin(targets = "net.minecraft.client.renderer.chunk.ChunkRenderDispatcher$RenderChunk")
public abstract class RenderChunkMixin implements RoadChunkBuffers {
  @Shadow @Final private Map<RenderType, VertexBuffer> buffers;

  @Override public Map<RenderType, VertexBuffer> splineroads$buffers() { return buffers; }

  @Inject(method = "reset", at = @At("HEAD"))
  private void sr$release(CallbackInfo info) {
    RoadTerrainHandoff.release(this);
  }
}
