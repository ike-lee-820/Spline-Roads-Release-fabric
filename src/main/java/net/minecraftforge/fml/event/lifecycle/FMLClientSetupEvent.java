package net.minecraftforge.fml.event.lifecycle;

import net.minecraftforge.eventbus.api.Event;

public final class FMLClientSetupEvent extends Event {
  public void enqueueWork(Runnable work) { work.run(); }
}
