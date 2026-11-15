package net.minecraftforge.event.entity.player;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.eventbus.api.Event;

public abstract class PlayerInteractEvent extends Event {
  private final Player player;
  private final InteractionHand hand;
  private final ItemStack stack;
  private InteractionResult cancellationResult = InteractionResult.PASS;
  protected PlayerInteractEvent(Player player, InteractionHand hand, ItemStack stack) {
    this.player = player; this.hand = hand; this.stack = stack;
  }
  public Player getEntity() { return player; }
  public InteractionHand getHand() { return hand; }
  public ItemStack getItemStack() { return stack; }
  public InteractionResult getCancellationResult() { return cancellationResult; }
  public void setCancellationResult(InteractionResult result) { this.cancellationResult = result; }

  public static class RightClickBlock extends PlayerInteractEvent {
    private final Level level;
    private final BlockPos pos;
    private final Direction face;
    private final BlockHitResult hit;
    public RightClickBlock(Level level, Player player, InteractionHand hand, ItemStack stack,
                           BlockPos pos, Direction face, BlockHitResult hit) {
      super(player, hand, stack);
      this.level = level; this.pos = pos; this.face = face; this.hit = hit;
    }
    public Level getLevel() { return level; }
    public BlockPos getPos() { return pos; }
    public Direction getFace() { return face; }
    public BlockHitResult getHitVec() { return hit; }
  }

  public static final class InteractionKeyMappingTriggered extends PlayerInteractEvent {
    private final boolean useItem;
    private boolean swingHand = true;
    public InteractionKeyMappingTriggered(Player player, InteractionHand hand, ItemStack stack, boolean useItem) {
      super(player, hand, stack); this.useItem = useItem;
    }
    public boolean isUseItem() { return useItem; }
    public boolean isSwingHand() { return swingHand; }
    public void setSwingHand(boolean value) { this.swingHand = value; }
  }
}
