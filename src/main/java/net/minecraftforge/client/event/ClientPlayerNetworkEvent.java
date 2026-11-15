package net.minecraftforge.client.event;

import net.minecraftforge.eventbus.api.Event;

public abstract class ClientPlayerNetworkEvent extends Event {
  public static final class LoggingOut extends ClientPlayerNetworkEvent {}
  public static final class LoggingIn extends ClientPlayerNetworkEvent {}
}
