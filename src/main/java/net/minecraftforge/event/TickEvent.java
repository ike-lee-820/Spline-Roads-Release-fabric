package net.minecraftforge.event;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.eventbus.api.Event;

public abstract class TickEvent extends Event {
  public enum Phase { START, END }
  public final Phase phase;
  protected TickEvent(Phase phase) { this.phase = phase; }
  public Phase getPhase() { return phase; }

  public static final class ClientTickEvent extends TickEvent {
    public ClientTickEvent(Phase phase) { super(phase); }
  }
  public static final class ServerTickEvent extends TickEvent {
    private final MinecraftServer server;
    public ServerTickEvent(Phase phase, MinecraftServer server) { super(phase); this.server = server; }
    public MinecraftServer getServer() { return server; }
  }
  public static final class RenderTickEvent extends TickEvent {
    public RenderTickEvent(Phase phase) { super(phase); }
  }
}
