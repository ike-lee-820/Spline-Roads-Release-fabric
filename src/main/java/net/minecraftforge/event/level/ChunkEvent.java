package net.minecraftforge.event.level;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.eventbus.api.Event;

public abstract class ChunkEvent extends Event {
  private final Level level;
  private final LevelChunk chunk;
  protected ChunkEvent(Level level, LevelChunk chunk) { this.level = level; this.chunk = chunk; }
  public Level getLevel() { return level; }
  public LevelChunk getChunk() { return chunk; }

  public static final class Load extends ChunkEvent {
    public Load(Level level, LevelChunk chunk) { super(level, chunk); }
  }
  public static final class Unload extends ChunkEvent {
    public Unload(Level level, LevelChunk chunk) { super(level, chunk); }
  }
}
