package com.sora.splineroads.fabric;

import java.lang.reflect.*;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.ForgeEventBus;

/**
 * Applies the original model-wrapping pass to vanilla's baked model map.
 * Fabric 1.20.1 has no direct equivalent of ModelEvent.ModifyBakingResult, so
 * the map is located reflectively and re-processed whenever it is replaced by
 * a resource reload.
 */
public final class ModelHook {
  private static volatile Map<?, ?> lastMap;

  public static void applyIfNeeded() {
    try {
      ModelManager manager = Minecraft.getInstance().getModelManager();
      if (manager == null) return;
      Object bakery = FabricApiBridge.field(manager, bakeryClass());
      Object source = bakery == null ? manager : bakery;
      for (Field f : source.getClass().getDeclaredFields()) {
        if (!Map.class.isAssignableFrom(f.getType())) continue;
        f.setAccessible(true);
        Object value = f.get(source);
        if (!(value instanceof Map<?, ?> map) || map.isEmpty()) continue;
        Object first = map.values().iterator().next();
        if (!(first instanceof BakedModel)) continue;
        if (map == lastMap) return;
        lastMap = map;
        @SuppressWarnings("unchecked")
        Map<ResourceLocation, BakedModel> models = (Map<ResourceLocation, BakedModel>) map;
        ForgeEventBus.post(new ModelEvent.ModifyBakingResult(models));
        ForgeEventBus.post(new ModelEvent.BakingCompleted(manager));
        return;
      }
    } catch (Throwable failure) {
      System.getLogger("SplineRoads/bridge").log(System.Logger.Level.WARNING,
          "Model baking hook unavailable", failure);
    }
  }

  private static Class<?> bakeryClass() {
    try { return Class.forName("net.minecraft.client.resources.model.ModelBakery"); }
    catch (Throwable absent) { return Object.class; }
  }
  private ModelHook() {}
}
