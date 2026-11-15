package net.minecraftforge.client.event;

import net.minecraftforge.eventbus.api.Event;

public abstract class InputEvent extends Event {
  public static final class Key extends InputEvent {
    private final int key, scanCode, action;
    public Key(int key, int scanCode, int action) { this.key = key; this.scanCode = scanCode; this.action = action; }
    public int getKey() { return key; }
    public int getScanCode() { return scanCode; }
    public int getAction() { return action; }
  }
  public static final class InteractionKeyMappingTriggered extends InputEvent {
    private final boolean useItem;
    private final net.minecraft.world.InteractionHand hand;
    private boolean swingHand = true;
    public InteractionKeyMappingTriggered(boolean useItem, net.minecraft.world.InteractionHand hand) {
      this.useItem = useItem; this.hand = hand;
    }
    public boolean isUseItem() { return useItem; }
    public net.minecraft.world.InteractionHand getHand() { return hand; }
    public boolean isSwingHand() { return swingHand; }
    public void setSwingHand(boolean value) { this.swingHand = value; }
  }
}
