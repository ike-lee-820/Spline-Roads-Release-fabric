package net.minecraftforge.client.event;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.eventbus.api.Event;

public final class RegisterKeyMappingsEvent extends Event {
  public void register(KeyMapping... mappings) {
    try {
      Class<?> helper = Class.forName("net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper");
      java.lang.reflect.Method method = helper.getMethod("registerKeyBinding", KeyMapping.class);
      for (KeyMapping mapping : mappings) method.invoke(null, mapping);
    } catch (Throwable failure) {
      System.getLogger("SplineRoads/bridge").log(System.Logger.Level.ERROR, "KeyBindingHelper unavailable", failure);
    }
  }
}
