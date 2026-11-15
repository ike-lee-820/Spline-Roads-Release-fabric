package net.minecraftforge.client.event;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.Event;

public abstract class RegisterColorHandlersEvent extends Event {
  public static final class Block extends RegisterColorHandlersEvent {
    public void register(BlockColor color, net.minecraft.world.level.block.Block... blocks) {
      register0(color, (Object[]) blocks);
    }
  }
  private static void register0(BlockColor color, Object... blocks) {
    try {
      Class<?> registry = Class.forName("net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry");
      java.lang.reflect.Field field = registry.getField("BLOCK");
      Object target = field.get(null);
      for (java.lang.reflect.Method m : target.getClass().getMethods()) {
        if (m.getName().equals("register") && m.getParameterCount() == 2) {
          Object array = java.lang.reflect.Array.newInstance(
              net.minecraft.world.level.block.Block.class, blocks.length);
          for (int i = 0; i < blocks.length; i++) java.lang.reflect.Array.set(array, i, blocks[i]);
          m.invoke(target, color, array);
          return;
        }
      }
    } catch (Throwable failure) {
      System.getLogger("SplineRoads/bridge").log(System.Logger.Level.ERROR,
          "ColorProviderRegistry unavailable", failure);
    }
  }
}
