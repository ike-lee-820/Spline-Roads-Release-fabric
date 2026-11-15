package net.minecraftforge.common;

/** Fabric port: EVENT_BUS is backed by {@link net.minecraftforge.eventbus.api.ForgeEventBus}. */
public final class MinecraftForge {
  public static final Bus EVENT_BUS = new Bus();

  public static final class Bus {
    public boolean post(Object event) {
      return net.minecraftforge.eventbus.api.ForgeEventBus.post(event);
    }
    public void register(Object target) {
      net.minecraftforge.eventbus.api.ForgeEventBus.register(target);
    }
  }
  private MinecraftForge() {}
}
