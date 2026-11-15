package net.minecraftforge.fml.javafmlmod;

import net.minecraftforge.eventbus.api.IEventBus;

public final class FMLJavaModLoadingContext {
  private static final FMLJavaModLoadingContext INSTANCE = new FMLJavaModLoadingContext();
  private static final IEventBus BUS = target -> {};

  public static FMLJavaModLoadingContext get() { return INSTANCE; }
  public IEventBus getModEventBus() { return BUS; }
  private FMLJavaModLoadingContext() {}
}
