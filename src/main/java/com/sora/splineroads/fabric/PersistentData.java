package com.sora.splineroads.fabric;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

/**
 * Fabric port of Forge's Entity.getPersistentData() for planner settings.
 * Values are mirrored into the player's saved NBT by PlayerDataMixin, so the
 * radius / undo / cache settings survive a server restart.
 */
public final class PersistentData {
  private static final Map<UUID, CompoundTag> DATA = new ConcurrentHashMap<>();

  public static CompoundTag of(ServerPlayer player) {
    return DATA.computeIfAbsent(player.getUUID(), key -> new CompoundTag());
  }
  public static CompoundTag snapshot(UUID id) {
    CompoundTag tag = DATA.get(id);
    return tag == null || tag.isEmpty() ? null : tag.copy();
  }
  public static void restore(UUID id, CompoundTag tag) {
    if (tag != null && !tag.isEmpty()) DATA.put(id, tag.copy());
  }
  public static void clear(UUID id) { DATA.remove(id); }
  public static void clear() { DATA.clear(); }
  private PersistentData() {}
}
