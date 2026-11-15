package net.minecraftforge.network;

import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.simple.SimpleChannel;

public final class NetworkRegistry {
  public static SimpleChannel newSimpleChannel(ResourceLocation name,
                                               Supplier<String> protocolVersion,
                                               Predicate<String> clientAccepted,
                                               Predicate<String> serverAccepted) {
    return new SimpleChannel(name);
  }
  private NetworkRegistry() {}
}
