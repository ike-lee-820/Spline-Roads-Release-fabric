package net.minecraftforge.client.event;

import com.mojang.blaze3d.vertex.VertexFormat;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraftforge.eventbus.api.Event;

/**
 * Fabric port of Forge's RegisterShadersEvent.
 * The preferred path delegates to Fabric API's CoreShaderRegistrationCallback,
 * which loads assets/&lt;namespace&gt;/shaders/core/*.json, adds the programs to
 * GameRenderer and reloads them with the vanilla shader cycle. The legacy
 * path (a ResourceManager) is kept for callers that still construct shaders
 * directly.
 */
public final class RegisterShadersEvent extends Event {
  private static final List<ShaderInstance> REGISTERED = new ArrayList<>();
  private final CoreShaderRegistrationCallback.RegistrationContext core;
  private final ResourceProvider provider;

  public RegisterShadersEvent(Object source) {
    if (source instanceof CoreShaderRegistrationCallback.RegistrationContext context) {
      this.core = context;
      this.provider = location -> Optional.empty();
    } else {
      this.core = null;
      ResourceManager manager = source instanceof ResourceManager ready ? ready : null;
      this.provider = location -> {
        ResourceLocation key = location;
        if ("minecraft".equals(key.getNamespace()) && key.getPath().startsWith("shaders/core/road_world_")) {
          key = new ResourceLocation("splineroads", key.getPath());
        }
        if (manager == null) return Optional.empty();
        return manager.getResource(key);
      };
    }
  }

  public ResourceProvider getResourceProvider() { return provider; }

  /** Registers a core shader through Fabric's reload-aware pipeline. */
  public void registerShader(ResourceLocation id, VertexFormat format,
                             Consumer<ShaderInstance> callback) throws IOException {
    if (core == null) {
      registerShader(new ShaderInstance(provider, id.getPath(), format), callback);
      return;
    }
    try {
      Method register = CoreShaderRegistrationCallback.RegistrationContext.class
          .getMethod("register", ResourceLocation.class, VertexFormat.class, Consumer.class);
      register.invoke(core, id, format, callback);
    } catch (InvocationTargetException failure) {
      Throwable cause = failure.getCause();
      if (cause instanceof IOException io) throw io;
      throw new IOException(cause == null ? failure : cause);
    } catch (ReflectiveOperationException failure) {
      throw new IOException(failure);
    }
  }

  public void registerShader(ShaderInstance shader, Consumer<ShaderInstance> consumer) {
    REGISTERED.add(shader);
    consumer.accept(shader);
  }
  public static List<ShaderInstance> registered() { return REGISTERED; }
}
