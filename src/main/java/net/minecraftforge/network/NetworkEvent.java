package net.minecraftforge.network;

import java.util.concurrent.Executor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class NetworkEvent {
  public static final class Context {
    private final MinecraftServer server;
    private final ServerPlayer sender;
    private final Executor executor;
    private volatile boolean handled;

    public Context(MinecraftServer server, ServerPlayer sender, Executor executor) {
      this.server = server; this.sender = sender; this.executor = executor;
    }
    public void enqueueWork(Runnable work) { executor.execute(work); }
    public void setPacketHandled(boolean value) { this.handled = value; }
    public boolean getPacketHandled() { return handled; }
    public ServerPlayer getSender() { return sender; }
    public MinecraftServer getServer() { return server; }
  }
  private NetworkEvent() {}
}
