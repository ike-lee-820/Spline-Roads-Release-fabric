package com.sora.splineroads.fabric;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.ForgeEventBus;

/** Server-side event mapping. Only common (non-client) Fabric APIs are linked. */
public final class FabricEventsCommon {
  public static void register() {
    ServerTickEvents.END_SERVER_TICK.register(server -> {
      ForgeEventBus.post(new TickEvent.ServerTickEvent(TickEvent.Phase.END, server));
      ChunkWatchTracker.tick(server);
    });
    ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
      ForgeEventBus.post(new ServerStoppingEvent(server));
      ChunkWatchTracker.clear();
    });
    ServerLifecycleEvents.SERVER_STOPPED.register(server ->
        ForgeEventBus.post(new ServerStoppedEvent(server)));
    ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
      ServerPlayer player = (ServerPlayer) FabricApiBridge.field(handler, ServerPlayer.class);
      if (player != null) ForgeEventBus.post(new PlayerEvent.PlayerLoggedOutEvent(player));
    });
    CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> {
      @SuppressWarnings("unchecked")
      CommandDispatcher<CommandSourceStack> typed =
          (CommandDispatcher<CommandSourceStack>) (Object) dispatcher;
      ForgeEventBus.post(new RegisterCommandsEvent(typed));
    });
    ServerChunkEvents.CHUNK_LOAD.register((world, chunk) -> postChunk(world, chunk, true));
    ServerChunkEvents.CHUNK_UNLOAD.register((world, chunk) -> postChunk(world, chunk, false));
    PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, entity) -> {
      Object[] args = {world, player, pos, state, entity};
      Level level = FabricApiBridge.arg(Level.class, args);
      Player who = FabricApiBridge.arg(Player.class, args);
      BlockPos at = FabricApiBridge.arg(BlockPos.class, args);
      BlockState block = FabricApiBridge.arg(BlockState.class, args);
      if (level == null || who == null || at == null || block == null) return true;
      BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(level, at, block, who);
      ForgeEventBus.post(event);
      return !event.isCanceled();
    });
    UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
      Object[] args = {player, world, hand, hitResult};
      Player who = FabricApiBridge.arg(Player.class, args);
      Level level = FabricApiBridge.arg(Level.class, args);
      InteractionHand used = FabricApiBridge.arg(InteractionHand.class, args);
      BlockHitResult hit = FabricApiBridge.arg(BlockHitResult.class, args);
      if (who == null || level == null || hit == null) return InteractionResult.PASS;
      InteractionHand actual = used == null ? InteractionHand.MAIN_HAND : used;
      PlayerInteractEvent.RightClickBlock event = new PlayerInteractEvent.RightClickBlock(
          level, who, actual, who.getItemInHand(actual),
          hit.getBlockPos(), hit.getDirection(), hit);
      ForgeEventBus.post(event);
      return event.isCanceled() ? event.getCancellationResult() : InteractionResult.PASS;
    });
    // Optional: dimension change is only a convenience for clearing drafts.
    FabricApiBridge.on("net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents", "AFTER_RESPAWN", args -> {
      ServerPlayer player = FabricApiBridge.arg(ServerPlayer.class, args);
      if (player != null) ForgeEventBus.post(new PlayerEvent.PlayerChangedDimensionEvent(player));
      return null;
    });
  }

  private static void postChunk(Object world, Object chunk, boolean load) {
    Level level = FabricApiBridge.arg(Level.class, new Object[]{world});
    LevelChunk section = FabricApiBridge.arg(LevelChunk.class, new Object[]{chunk});
    if (level == null || section == null) return;
    ForgeEventBus.post(load ? new ChunkEvent.Load(level, section)
                            : new ChunkEvent.Unload(level, section));
  }
  private FabricEventsCommon() {}
}
