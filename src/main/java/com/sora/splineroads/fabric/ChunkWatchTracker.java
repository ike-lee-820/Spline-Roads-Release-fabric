package com.sora.splineroads.fabric;

import java.util.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.event.level.ChunkWatchEvent;
import net.minecraftforge.eventbus.api.ForgeEventBus;

/**
 * Approximates Forge's ChunkWatchEvent.Watch / UnWatch from each player's
 * tracking radius. RoadNetwork.watch() only needs the set of visible chunks,
 * which this reproduces without a mixin into the chunk tracker.
 */
public final class ChunkWatchTracker {
  private static final Map<UUID, Set<Long>> LAST = new HashMap<>();

  public static void tick(MinecraftServer server) {
    if (server == null || server.getTickCount() % 5 != 0) return;
    int radius = Math.max(2, server.getPlayerList().getViewDistance());
    Set<UUID> online = new HashSet<>();
    for (ServerPlayer player : server.getPlayerList().getPlayers()) {
      UUID id = player.getUUID();
      online.add(id);
      int cx = ((int) Math.floor(player.getX())) >> 4;
      int cz = ((int) Math.floor(player.getZ())) >> 4;
      Set<Long> now = new HashSet<>();
      for (int x = -radius; x <= radius; x++)
        for (int z = -radius; z <= radius; z++)
          now.add(ChunkPos.asLong(cx + x, cz + z));
      Set<Long> previous = LAST.getOrDefault(id, Set.of());
      ServerLevel level = player.serverLevel();
      for (long packed : now) if (!previous.contains(packed))
        ForgeEventBus.post(new ChunkWatchEvent.Watch(player, unpack(packed), level));
      for (long packed : previous) if (!now.contains(packed))
        ForgeEventBus.post(new ChunkWatchEvent.UnWatch(player, unpack(packed), level));
      LAST.put(id, now);
    }
    LAST.keySet().retainAll(online);
  }

  private static ChunkPos unpack(long packed) {
    return new ChunkPos((int) packed, (int) (packed >> 32));
  }
  public static void clear() { LAST.clear(); }
  private ChunkWatchTracker() {}
}
