package net.minecraftforge.client.event;

import java.util.*;
import java.util.function.Consumer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraftforge.eventbus.api.Event;
import net.minecraft.server.packs.resources.Resource;

/** Fabric port: shaders are constructed by the mod and handed to the callback. */
public final class RegisterShadersEvent extends Event {
  private static final List<ShaderInstance> REGISTERED = new ArrayList<>();
  private final ResourceProvider provider;

  public RegisterShadersEvent(Object source) {
    if (source instanceof ResourceProvider ready) {
      this.provider = ready;
    } else {
      // ResourceManager exposes getResource(ResourceLocation) -> Optional<Resource>.
      this.provider = location -> {
        try {
          java.lang.reflect.Method getter = source.getClass().getMethod("getResource", ResourceLocation.class);
          Object optional = getter.invoke(source, location);
          java.lang.reflect.Method orElse = optional.getClass().getMethod("orElse", Object.class);
          Object resource = orElse.invoke(optional, (Object) null);
          return (Resource) resource;
        } catch (java.lang.reflect.InvocationTargetException wrapped) {
          if (wrapped.getCause() instanceof java.io.IOException io) throw io;
          throw new java.io.UncheckedIOException(new java.io.IOException(wrapped.getCause()));
        } catch (Throwable failure) {
          throw new java.io.FileNotFoundException(String.valueOf(location));
        }
      };
    }
  }

  public ResourceProvider getResourceProvider() { return provider; }
  public void registerShader(ShaderInstance shader, Consumer<ShaderInstance> consumer) {
    REGISTERED.add(shader);
    consumer.accept(shader);
  }
  public static List<ShaderInstance> registered() { return REGISTERED; }
}
