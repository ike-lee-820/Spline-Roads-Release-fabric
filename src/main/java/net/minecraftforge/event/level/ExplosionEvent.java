package net.minecraftforge.event.level;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Event;

public abstract class ExplosionEvent extends Event {
  private final Level level;
  protected ExplosionEvent(Level level) { this.level = level; }
  public Level getLevel() { return level; }

  public static final class Detonate extends ExplosionEvent {
    private final List<BlockPos> affected;
    public Detonate(Level level, List<BlockPos> affected) {
      super(level); this.affected = new ArrayList<>(affected);
    }
    public List<BlockPos> getAffectedBlocks() { return affected; }
  }
}
