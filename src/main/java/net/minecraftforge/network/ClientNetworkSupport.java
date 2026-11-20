package net.minecraftforge.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/** Client-only half of the Forge SimpleChannel bridge. Never loaded on a dedicated server. */
public final class ClientNetworkSupport {

  public static void register(ResourceLocation id, FabricNetworkSupport.Receiver receiver) {
    ClientPlayNetworking.registerGlobalReceiver(id,
        (client, handler, buf, responseSender) -> receiver.receive(buf, null, null));
  }

  public static void send(ResourceLocation id, FriendlyByteBuf buf) {
    ClientPlayNetworking.send(id, buf);
  }

  public static String dimension() {
    var level = net.minecraft.client.Minecraft.getInstance().level;
    return level == null ? null : level.dimension().location().toString();
  }

  private ClientNetworkSupport() {}
}
