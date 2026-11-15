package com.sora.splineroads.fabric;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

/**
 * Fabric port of Forge's Entity.getPersistentData() for the planner settings.
 * Values live for the lifetime of the server process (they are not written to
 * player NBT); see MIGRATION.md.
 */
public final class PersistentData {
  private static final Map<UUID, CompoundTag> DATA = new ConcurrentHashMap<>();

  public static CompoundTag of(ServerPlayer player) {
    return DATA.computeIfAbsent(player.getUUID(), key -> new CompoundTag());
  }
  public static void clear(UUID id) { DATA.remove(id); }
  public static void clear() { DATA.clear(); }
  private PersistentData() {}
}
