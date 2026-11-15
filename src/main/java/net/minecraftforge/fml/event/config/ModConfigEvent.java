package net.minecraftforge.fml.event.config;

import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.config.ModConfig;

public abstract class ModConfigEvent extends Event {
  private final ModConfig config;
  protected ModConfigEvent(ModConfig config) { this.config = config; }
  public ModConfig getConfig() { return config; }

  public static final class Loading extends ModConfigEvent {
    public Loading(ModConfig config) { super(config); }
  }
  public static final class Reloading extends ModConfigEvent {
    public Reloading(ModConfig config) { super(config); }
  }
}
