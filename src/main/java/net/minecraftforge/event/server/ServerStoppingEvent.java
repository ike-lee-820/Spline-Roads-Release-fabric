package net.minecraftforge.event.server;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.eventbus.api.Event;

public final class ServerStoppingEvent extends Event {
  private final MinecraftServer server;
  public ServerStoppingEvent(MinecraftServer server) { this.server = server; }
  public MinecraftServer getServer() { return server; }
}
