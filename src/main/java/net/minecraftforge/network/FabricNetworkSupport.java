package net.minecraftforge.network;

import java.lang.reflect.*;
import java.util.function.BiConsumer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * Fabric port: all Fabric Networking API calls are resolved reflectively so the
 * common initializer never links client-only classes on a dedicated server.
 */
public final class FabricNetworkSupport {
  public interface Receiver {
    void receive(FriendlyByteBuf buf, MinecraftServer server, ServerPlayer player);
  }

  private static final String SERVER = "net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking";
  private static final String CLIENT = "net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking";

  public static void sendToPlayer(ServerPlayer player, ResourceLocation id, FriendlyByteBuf buf) {
    try {
      Class<?> api = Class.forName(SERVER);
      for (Method m : api.getMethods()) {
        if (m.getName().equals("send") && m.getParameterCount() == 3) {
          m.invoke(null, player, id, buf);
          return;
        }
      }
      throw new IllegalStateException("ServerPlayNetworking.send not found");
    } catch (ReflectiveOperationException failure) {
      throw new IllegalStateException("Fabric networking API unavailable", failure);
    }
  }

  public static void sendToServer(ResourceLocation id, FriendlyByteBuf buf) {
    try {
      Class<?> api = Class.forName(CLIENT);
      for (Method m : api.getMethods()) {
        if (m.getName().equals("send") && m.getParameterCount() == 2) {
          m.invoke(null, id, buf);
          return;
        }
      }
      throw new IllegalStateException("ClientPlayNetworking.send not found");
    } catch (ReflectiveOperationException failure) {
      throw new IllegalStateException("Fabric client networking API unavailable", failure);
    }
  }

  private static void register(String className, ResourceLocation id, Receiver receiver, boolean serverSide) {
    try {
      Class<?> api = Class.forName(className);
      Method target = null;
      for (Method m : api.getMethods()) {
        if (m.getName().equals("registerGlobalReceiver") && m.getParameterCount() == 2) {
          if (target == null || m.getParameterTypes()[1].isInterface()) target = m;
        }
      }
      if (target == null) throw new NoSuchMethodException("registerGlobalReceiver");
      Class<?> listener = target.getParameterTypes()[1];
      Object proxy = Proxy.newProxyInstance(FabricNetworkSupport.class.getClassLoader(),
          new Class<?>[]{listener}, (p, method, args) -> {
            if (method.getDeclaringClass() == Object.class) {
              return switch (method.getName()) {
                case "hashCode" -> System.identityHashCode(p);
                case "equals" -> p == args[0];
                default -> "SplineRoads channel";
              };
            }
            FriendlyByteBuf buf = null;
            MinecraftServer server = null;
            ServerPlayer player = null;
            if (args != null) for (Object arg : args) {
              if (arg instanceof FriendlyByteBuf b) buf = b;
              else if (arg instanceof MinecraftServer s) server = s;
              else if (arg instanceof ServerPlayer sp) player = sp;
            }
            if (buf != null) receiver.receive(buf, server, player);
            return null;
          });
      target.invoke(null, id, proxy);
    } catch (Throwable failure) {
      System.getLogger("SplineRoads/net").log(System.Logger.Level.ERROR,
          "Cannot register channel " + id + " (" + className + ")", failure);
    }
  }

  public static void registerServerReceiver(ResourceLocation id, Receiver receiver) {
    register(SERVER, id, receiver, true);
  }
  public static void registerClientReceiver(ResourceLocation id, Receiver receiver) {
    register(CLIENT, id, receiver, false);
  }
  private FabricNetworkSupport() {}
}
