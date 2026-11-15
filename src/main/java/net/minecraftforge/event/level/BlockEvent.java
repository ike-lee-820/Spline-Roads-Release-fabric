package net.minecraftforge.event.level;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.eventbus.api.Event;

public abstract class BlockEvent extends Event {
  private final Level level;
  private final BlockPos pos;
  private final BlockState state;
  protected BlockEvent(Level level, BlockPos pos, BlockState state) {
    this.level = level; this.pos = pos; this.state = state;
  }
  public Level getLevel() { return level; }
  public BlockPos getPos() { return pos; }
  public BlockState getState() { return state; }

  public static class BreakEvent extends BlockEvent {
    private final Player player;
    public BreakEvent(Level level, BlockPos pos, BlockState state, Player player) {
      super(level, pos, state); this.player = player;
    }
    public Player getPlayer() { return player; }
  }

  public static class EntityPlaceEvent extends BlockEvent {
    private final BlockSnapshot snapshot;
    private final Player player;
    public EntityPlaceEvent(BlockSnapshot snapshot, BlockState placedAgainst, Player player) {
      super((Level) null, snapshot == null ? BlockPos.ZERO : snapshot.getPos(), placedAgainst);
      this.snapshot = snapshot; this.player = player;
    }
    public BlockSnapshot getSnapshot() { return snapshot; }
    public Player getPlayer() { return player; }
    public BlockState getPlacedBlock() { return getState(); }
  }
}
