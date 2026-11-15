package net.minecraftforge.client.event;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.Event;

public abstract class EntityRenderersEvent extends Event {
  public static final class RegisterRenderers extends EntityRenderersEvent {
    public <T extends BlockEntity> void registerBlockEntityRenderer(
        BlockEntityType<T> type, BlockEntityRendererProvider<T> provider) {
      try {
        Class<?> registry = Class.forName("net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry");
        java.lang.reflect.Method method = null;
        for (java.lang.reflect.Method m : registry.getDeclaredMethods()) {
          if (m.getName().equals("register") && m.getParameterCount() == 2) method = m;
        }
        if (method != null) method.invoke(null, type, provider);
      } catch (Throwable failure) {
        System.getLogger("SplineRoads/bridge").log(System.Logger.Level.ERROR,
            "BlockEntityRendererRegistry unavailable", failure);
      }
    }
  }
}
