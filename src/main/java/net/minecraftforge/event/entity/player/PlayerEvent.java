package net.minecraftforge.event.entity.player;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event;

public abstract class PlayerEvent extends Event {
  private final Player player;
  protected PlayerEvent(Player player) { this.player = player; }
  public Player getEntity() { return player; }

  public static final class PlayerLoggedOutEvent extends PlayerEvent {
    public PlayerLoggedOutEvent(Player player) { super(player); }
  }
  public static final class PlayerChangedDimensionEvent extends PlayerEvent {
    public PlayerChangedDimensionEvent(Player player) { super(player); }
  }
  public static final class PlayerLoggedInEvent extends PlayerEvent {
    public PlayerLoggedInEvent(Player player) { super(player); }
  }
}
