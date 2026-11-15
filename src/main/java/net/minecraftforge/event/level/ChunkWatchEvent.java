package net.minecraftforge.event.level;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.eventbus.api.Event;

/** Synthesized by the Fabric bridge from each player's chunk tracking view. */
public abstract class ChunkWatchEvent extends Event {
  private final ServerPlayer player;
  private final ChunkPos pos;
  private final ServerLevel level;
  protected ChunkWatchEvent(ServerPlayer player, ChunkPos pos, ServerLevel level) {
    this.player = player; this.pos = pos; this.level = level;
  }
  public ServerPlayer getPlayer() { return player; }
  public ChunkPos getPos() { return pos; }
  public ServerLevel getLevel() { return level; }

  public static final class Watch extends ChunkWatchEvent {
    public Watch(ServerPlayer p, ChunkPos c, ServerLevel l) { super(p, c, l); }
  }
  public static final class UnWatch extends ChunkWatchEvent {
    public UnWatch(ServerPlayer p, ChunkPos c, ServerLevel l) { super(p, c, l); }
  }
}
