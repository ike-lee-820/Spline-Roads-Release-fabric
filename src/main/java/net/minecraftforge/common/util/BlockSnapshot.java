package net.minecraftforge.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

/** Fabric port: a lightweight immutable description of a block location. */
public final class BlockSnapshot {
  private final ResourceKey<Level> dimension;
  private final BlockPos pos;
  private final BlockState state;

  private BlockSnapshot(ResourceKey<Level> dimension, BlockPos pos, BlockState state) {
    this.dimension = dimension; this.pos = pos; this.state = state;
  }

  public static BlockSnapshot create(ResourceKey<Level> dimension, LevelAccessor level, BlockPos pos) {
    return new BlockSnapshot(dimension, pos.immutable(), level.getBlockState(pos));
  }

  public ResourceKey<Level> getDimension() { return dimension; }
  public BlockPos getPos() { return pos; }
  public BlockState getState() { return state; }
  public BlockState getReplacedBlock() { return state; }
}
