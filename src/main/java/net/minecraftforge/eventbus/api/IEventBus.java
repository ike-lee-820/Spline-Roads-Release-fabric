package net.minecraftforge.eventbus.api;

/** Deferred registration is eager on Fabric, so this is a no-op handle. */
public interface IEventBus {
  void register(Object target);
}
