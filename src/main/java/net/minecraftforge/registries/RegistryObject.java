package net.minecraftforge.registries;

import java.util.function.Supplier;

/** Fabric port: registration is eager, so this simply holds the object. */
public final class RegistryObject<T> implements Supplier<T> {
  private final T value;
  public RegistryObject(T value) { this.value = value; }
  public T get() { return value; }
  public boolean isPresent() { return value != null; }
  @Override public String toString() { return "RegistryObject[" + value + "]"; }
}
