package net.minecraftforge.fml.config;

public final class IConfigEvent {
  private final Object config;
  private IConfigEvent(Object config) { this.config = config; }
  public static IConfigEvent reloading(Object config) { return new IConfigEvent(config); }
  public Object getConfig() { return config; }
}
