package net.minecraftforge.network.simple;

import java.util.*;
import java.util.function.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.FabricNetworkSupport;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

/** Fabric port of Forge's SimpleChannel over Fabric's custom payload channels. */
public final class SimpleChannel {
  private static final List<Runnable> CLIENT_REGISTRATIONS = new ArrayList<>();

  private final ResourceLocation base;
  private final Map<Class<?>, Entry<?>> entries = new HashMap<>();

  private static final class Entry<M> {
    final ResourceLocation id;
    final BiConsumer<M, FriendlyByteBuf> encoder;
    final Function<FriendlyByteBuf, M> decoder;
    final BiConsumer<M, Supplier<NetworkEvent.Context>> handler;
    final boolean toServer;
    Entry(ResourceLocation id, BiConsumer<M, FriendlyByteBuf> encoder,
          Function<FriendlyByteBuf, M> decoder,
          BiConsumer<M, Supplier<NetworkEvent.Context>> handler, boolean toServer) {
      this.id = id; this.encoder = encoder; this.decoder = decoder;
      this.handler = handler; this.toServer = toServer;
    }
  }

  public SimpleChannel(ResourceLocation base) { this.base = base; }

  private ResourceLocation messageId(int index) {
    return new ResourceLocation(base.getNamespace(), base.getPath() + "_" + index);
  }

  public <M> void registerMessage(int index, Class<M> type,
                                  BiConsumer<M, FriendlyByteBuf> encoder,
                                  Function<FriendlyByteBuf, M> decoder,
                                  BiConsumer<M, Supplier<NetworkEvent.Context>> handler,
                                  Optional<NetworkDirection> direction) {
    boolean toServer = direction.isPresent() && direction.get() == NetworkDirection.PLAY_TO_SERVER;
    ResourceLocation id = messageId(index);
    Entry<M> entry = new Entry<>(id, encoder, decoder, handler, toServer);
    entries.put(type, entry);

    if (toServer) {
      FabricNetworkSupport.registerServerReceiver(id, (buf, server, player) -> {
        M message = decoder.apply(buf);
        NetworkEvent.Context context = new NetworkEvent.Context(server, player,
            server == null ? Runnable::run : server::execute);
        handler.accept(message, () -> context);
      });
    } else {
      CLIENT_REGISTRATIONS.add(() -> FabricNetworkSupport.registerClientReceiver(id, (buf, server, player) -> {
        M message = decoder.apply(buf);
        java.util.concurrent.Executor executor = clientExecutor();
        NetworkEvent.Context context = new NetworkEvent.Context(null, null, executor);
        handler.accept(message, () -> context);
      }));
    }
  }

  private static java.util.concurrent.Executor clientExecutor() {
    try {
      Class<?> mc = Class.forName("net.minecraft.client.Minecraft");
      Object instance = mc.getMethod("getInstance").invoke(null);
      java.lang.reflect.Method execute = mc.getMethod("execute", Runnable.class);
      return command -> {
        try { execute.invoke(instance, command); }
        catch (ReflectiveOperationException bad) { command.run(); }
      };
    } catch (Throwable absent) { return Runnable::run; }
  }

  /** Called from the client entrypoint once client-only classes are safe to load. */
  public static void registerClientReceivers() {
    for (Runnable registration : CLIENT_REGISTRATIONS) registration.run();
    CLIENT_REGISTRATIONS.clear();
  }

  @SuppressWarnings("unchecked")
  private <M> Entry<M> entryOf(M message) {
    Entry<?> found = entries.get(message.getClass());
    if (found == null) throw new IllegalStateException("Unregistered packet: " + message.getClass());
    return (Entry<M>) found;
  }

  public <M> void send(PacketDistributor.PacketTarget target, M message) {
    Entry<M> entry = entryOf(message);
    FriendlyByteBuf buf = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
    entry.encoder.accept(message, buf);
    FabricNetworkSupport.sendToPlayer((ServerPlayer) target.target(), entry.id, buf);
  }

  public <M> void sendToServer(M message) {
    Entry<M> entry = entryOf(message);
    FriendlyByteBuf buf = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
    entry.encoder.accept(message, buf);
    FabricNetworkSupport.sendToServer(entry.id, buf);
  }
}
