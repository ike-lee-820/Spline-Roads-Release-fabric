package net.minecraftforge.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * Fabric port of Forge's SimpleChannel networking.
 *
 * Server-side calls are linked at compile time so a Fabric API signature
 * mismatch fails the build instead of silently dropping every packet. The
 * client-only half lives in ClientNetworkSupport and is reached by name, so a
 * dedicated server never links net.minecraft.client classes.
 */
public final class FabricNetworkSupport {
  private static final String LOG = "SplineRoads/net";

  public interface Receiver {
    void receive(FriendlyByteBuf buf, MinecraftServer server, ServerPlayer player);
  }

  public static void sendToPlayer(ServerPlayer player, ResourceLocation id, FriendlyByteBuf buf) {
    ServerPlayNetworking.send(player, id, buf);
  }

  public static void registerServerReceiver(ResourceLocation id, Receiver receiver) {
    ServerPlayNetworking.registerGlobalReceiver(id,
        (server, player, handler, buf, responseSender) -> {
          if (player == null) {
            System.getLogger(LOG).log(System.Logger.Level.ERROR,
                "Server packet on " + id + " arrived without a player; action ignored");
            return;
          }
          receiver.receive(buf, server, player);
        });
  }

  /** Reached only from the client entrypoint. */
  public static void registerClientReceiver(ResourceLocation id, Receiver receiver) {
    try {
      Class<?> helper = Class.forName("net.minecraftforge.network.ClientNetworkSupport");
      helper.getMethod("register", ResourceLocation.class, Receiver.class).invoke(null, id, receiver);
    } catch (Throwable failure) {
      throw new IllegalStateException("Cannot register client channel " + id, failure);
    }
  }

  public static void sendToServer(ResourceLocation id, FriendlyByteBuf buf) {
    try {
      Class<?> helper = Class.forName("net.minecraftforge.network.ClientNetworkSupport");
      helper.getMethod("send", ResourceLocation.class, FriendlyByteBuf.class)
          .invoke(null, id, buf);
    } catch (Throwable failure) {
      throw new IllegalStateException("Fabric client networking unavailable", failure);
    }
  }

  /** Returns the client world dimension id, or null on a dedicated server / no world. */
  public static String clientDimension() {
    try {
      Class<?> helper = Class.forName("net.minecraftforge.network.ClientNetworkSupport");
      return (String) helper.getMethod("dimension").invoke(null);
    } catch (Throwable absent) {
      return null;
    }
  }

  private FabricNetworkSupport() {}
}
