package net.minecraftforge.client.event;

import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.eventbus.api.Event;

public abstract class ScreenEvent extends Event {
  private final Screen oldScreen;
  protected ScreenEvent(Screen oldScreen) { this.oldScreen = oldScreen; }
  public Screen getScreen() { return oldScreen; }

  public static final class Opening extends ScreenEvent {
    private Screen newScreen;
    public Opening(Screen oldScreen, Screen newScreen) { super(oldScreen); this.newScreen = newScreen; }
    public Screen getNewScreen() { return newScreen; }
    public void setNewScreen(Screen screen) { this.newScreen = screen; }
  }
}
