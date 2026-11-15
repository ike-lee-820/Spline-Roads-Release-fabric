package net.minecraftforge.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraftforge.eventbus.api.Event;
import org.joml.Matrix4f;

/** Fabric port: carries the WorldRenderContext state the renderer needs. */
public final class RenderLevelStageEvent extends Event {
  public enum Stage {
    AFTER_SKY, AFTER_SOLID_BLOCKS, AFTER_CUTOUT_BLOCKS, AFTER_BLOCK_ENTITIES,
    AFTER_TRANSLUCENT_BLOCKS, AFTER_TRIPWIRE_BLOCKS, AFTER_PARTICLES, AFTER_WEATHER, AFTER_LEVEL
  }
  private final Stage stage;
  private final PoseStack pose;
  private final Camera camera;
  private final Matrix4f projection;
  private final Frustum frustum;
  private final float tickDelta;

  public RenderLevelStageEvent(Stage stage, PoseStack pose, Camera camera, Matrix4f projection,
                               Frustum frustum, float tickDelta) {
    this.stage = stage; this.pose = pose; this.camera = camera;
    this.projection = projection; this.frustum = frustum; this.tickDelta = tickDelta;
  }
  public Stage getStage() { return stage; }
  public PoseStack getPoseStack() { return pose; }
  public Camera getCamera() { return camera; }
  public Matrix4f getProjectionMatrix() { return projection; }
  public Frustum getFrustum() { return frustum; }
  public float getPartialTick() { return tickDelta; }
}
