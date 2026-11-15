package net.minecraftforge.network;

import java.util.function.Supplier;

public final class PacketDistributor<T> {
  public static final PacketDistributor<net.minecraft.server.level.ServerPlayer> PLAYER =
      new PacketDistributor<>();

  private PacketDistributor() {}

  public PacketTarget with(Supplier<T> supplier) { return new PacketTarget(supplier.get()); }

  public static final class PacketTarget {
    private final Object target;
    PacketTarget(Object target) { this.target = target; }
    public Object target() { return target; }
  }
}
