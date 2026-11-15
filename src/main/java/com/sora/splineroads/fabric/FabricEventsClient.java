package com.sora.splineroads.fabric;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.ForgeEventBus;
import org.joml.Matrix4f;

/** Client-side event mapping. Arguments are extracted reflectively for resilience. */
public final class FabricEventsClient {
  public static void register() {
    // Fabric's core-shader callback is the equivalent of Forge's
    // RegisterShadersEvent: the resource provider is live and reloads are
    // handled by GameRenderer instead of a one-off startup guess.
    CoreShaderRegistrationCallback.EVENT.register(context ->
        ForgeEventBus.post(new RegisterShadersEvent(context)));
    ClientTickEvents.END_CLIENT_TICK.register(client -> {
      ForgeEventBus.post(new TickEvent.ClientTickEvent(TickEvent.Phase.END));
      if (Minecraft.getInstance().level != null) ModelHook.applyIfNeeded();
    });
    WorldRenderEvents.AFTER_ENTITIES.register(context ->
        ForgeEventBus.post(renderEvent(context, RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES)));
    WorldRenderEvents.END.register(context ->
        ForgeEventBus.post(renderEvent(context, RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS)));
    HudRenderCallback.EVENT.register((graphics, delta) -> {
      ForgeEventBus.post(new CustomizeGuiOverlayEvent.DebugText());
      Object[] args = {graphics};
      GuiGraphics g = FabricApiBridge.arg(GuiGraphics.class, args);
      if (g != null) ForgeEventBus.post(new RenderGuiOverlayEvent.Post(g,
          new RenderGuiOverlayEvent.Overlay(new ResourceLocation("minecraft", "crosshair"))));
    });
    ClientPlayConnectionEvents.DISCONNECT.register((handler, client) ->
        ForgeEventBus.post(new ClientPlayerNetworkEvent.LoggingOut()));
    ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) -> {
      @SuppressWarnings("unchecked")
      CommandDispatcher<CommandSourceStack> typed =
          (CommandDispatcher<CommandSourceStack>) (Object) dispatcher;
      ForgeEventBus.post(new RegisterClientCommandsEvent(typed));
    });

    // Optional hooks: keep them non-fatal if this Fabric API build differs.
    FabricApiBridge.on("net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents", "START", args ->
        { ForgeEventBus.post(new TickEvent.RenderTickEvent(TickEvent.Phase.START)); return null; });
    FabricApiBridge.on("net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents",
        "CLIENT_STARTED", args -> { startup(); return null; });
    FabricApiBridge.on("net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents",
        "CHUNK_LOAD", args -> { clientChunk(args, true); return null; });
    FabricApiBridge.on("net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents",
        "CHUNK_UNLOAD", args -> { clientChunk(args, false); return null; });
    startup();
  }

  private static void startup() {
    ModelHook.applyIfNeeded();
  }

  private static void clientChunk(Object[] args, boolean load) {
    net.minecraft.world.level.Level level = FabricApiBridge.arg(net.minecraft.world.level.Level.class, args);
    net.minecraft.world.level.chunk.LevelChunk chunk =
        FabricApiBridge.arg(net.minecraft.world.level.chunk.LevelChunk.class, args);
    if (level == null || chunk == null) return;
    ForgeEventBus.post(load ? new net.minecraftforge.event.level.ChunkEvent.Load(level, chunk)
                            : new net.minecraftforge.event.level.ChunkEvent.Unload(level, chunk));
  }

  private static RenderLevelStageEvent renderEvent(Object context, RenderLevelStageEvent.Stage stage) {
    PoseStack pose = (PoseStack) FabricApiBridge.call(context, "matrixStack", "poseStack", "matrices");
    Camera camera = (Camera) FabricApiBridge.call(context, "camera");
    Matrix4f projection = (Matrix4f) FabricApiBridge.call(context, "projectionMatrix", "projection");
    Frustum frustum = (Frustum) FabricApiBridge.call(context, "frustum");
    Object partial = FabricApiBridge.call(context, "tickDelta", "delta", "tickCounter");
    float delta = partial instanceof Number number ? number.floatValue() : 0f;
    if (camera == null) {
      Object renderer = FabricApiBridge.call(Minecraft.getInstance(), "getGameRenderer", "gameRenderer");
      camera = (Camera) FabricApiBridge.call(renderer, "getMainCamera");
      if (camera == null) camera = (Camera) FabricApiBridge.field(renderer, Camera.class);
    }
    if (camera == null) camera = new Camera();
    if (projection == null) {
      try { projection = com.mojang.blaze3d.systems.RenderSystem.getProjectionMatrix(); }
      catch (Throwable ignored) { projection = new Matrix4f(); }
    }
    if (pose == null) pose = new PoseStack();
    if (frustum == null) {
      Object renderer = FabricApiBridge.call(Minecraft.getInstance(), "getLevelRenderer", "levelRenderer");
      frustum = (Frustum) FabricApiBridge.field(renderer, Frustum.class);
    }
    if (frustum == null) frustum = new VisibleFrustum();
    return new RenderLevelStageEvent(stage, pose, camera, projection, frustum, delta);
  }

  /** Fallback so sign/toll visibility checks never NPE when the API omits a frustum. */
  private static final class VisibleFrustum extends Frustum {
    VisibleFrustum() { super(new Matrix4f(), new Matrix4f()); }
    @Override public boolean isVisible(net.minecraft.world.phys.AABB bounds) { return true; }
  }
  private FabricEventsClient() {}
}
