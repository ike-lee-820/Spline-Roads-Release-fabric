package net.minecraftforge.client.event;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraftforge.eventbus.api.Event;

/**
 * Fabric port of Forge's RegisterShadersEvent.
 * On 1.20.1 vanilla, ShaderInstance takes a String shader name and resolves
 * {@code minecraft:shaders/core/<name>.json}; our shader JSONs live in the
 * mod namespace, so the provider transparently rewrites only those lookups.
 */
public final class RegisterShadersEvent extends Event {
  private static final List<ShaderInstance> REGISTERED = new ArrayList<>();
  private final ResourceProvider provider;

  public RegisterShadersEvent(Object source) {
    ResourceManager manager = source instanceof ResourceManager ready ? ready : null;
    this.provider = location -> {
      ResourceLocation key = location;
      if ("minecraft".equals(key.getNamespace()) && key.getPath().startsWith("shaders/core/road_world_")) {
        key = new ResourceLocation("splineroads", key.getPath());
      }
      try {
        if (manager != null) return manager.getResource(key);
        Method getter = source.getClass().getMethod("getResource", ResourceLocation.class);
        Object value = getter.invoke(source, key);
        return value instanceof Optional<?> optional
            ? (Optional<Resource>) optional
            : Optional.empty();
      } catch (Throwable failure) {
        return Optional.empty();
      }
    };
  }

  public ResourceProvider getResourceProvider() { return provider; }
  public void registerShader(ShaderInstance shader, Consumer<ShaderInstance> consumer) {
    REGISTERED.add(shader);
    consumer.accept(shader);
  }
  public static List<ShaderInstance> registered() { return REGISTERED; }
}
