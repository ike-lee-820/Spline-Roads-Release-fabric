package net.minecraftforge.registries;

import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;

/**
 * Fabric port: entries are registered immediately with Registry.register(), so
 * RegistryObject.get() is valid during static initialization just like Forge.
 */
public final class DeferredRegister<T> {
  private final Registry<T> registry;
  private final String modid;

  private DeferredRegister(Registry<T> registry, String modid) {
    this.registry = registry;
    this.modid = modid;
  }

  public static <T> DeferredRegister<T> create(Registry<T> registry, String modid) {
    return new DeferredRegister<>(registry, modid);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public static <T> DeferredRegister<T> create(ResourceKey<? extends Registry<T>> key, String modid) {
    Registry<T> registry;
    if ("creative_mode_tab".equals(key.location().getPath())) {
      registry = (Registry<T>) BuiltInRegistries.CREATIVE_MODE_TAB;
    } else {
      registry = (Registry<T>) BuiltInRegistries.REGISTRY.get(key.location());
    }
    return new DeferredRegister<>(registry, modid);
  }

  public <I extends T> RegistryObject<I> register(String name, Supplier<? extends I> supplier) {
    I value = supplier.get();
    ResourceLocation id = new ResourceLocation(modid, name);
    Registry.register(registry, id, value);
    return new RegistryObject<>(value);
  }

  /** Registration already happened; retained for source compatibility. */
  public void register(IEventBus bus) { }
}
