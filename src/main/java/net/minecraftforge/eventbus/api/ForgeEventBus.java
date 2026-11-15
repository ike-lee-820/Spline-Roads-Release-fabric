package net.minecraftforge.eventbus.api;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Fabric port: replaces Forge's event bus with an explicit dispatcher.
 * Subscriber classes are listed (rather than class-path scanned) so loading is
 * deterministic under Fabric's transforming class loader. Classes that fail to
 * load on a dedicated server are retried from the client entrypoint.
 */
public final class ForgeEventBus {
  private static final String[] SUBSCRIBERS = {
      "com.sora.splineroads.client.AttachedPointRenderer",
      "com.sora.splineroads.client.ClientRoads",
      "com.sora.splineroads.client.ClientRoads$Setup",
      "com.sora.splineroads.client.LanePointRenderer",
      "com.sora.splineroads.client.ManualPlannerClient",
      "com.sora.splineroads.client.ManualPlannerClient$Setup",
      "com.sora.splineroads.client.NodeRenderer",
      "com.sora.splineroads.client.RailbedHighlight",
      "com.sora.splineroads.client.RoadClientCommands",
      "com.sora.splineroads.client.RoadClientConfigReload",
      "com.sora.splineroads.client.RoadRenderer",
      "com.sora.splineroads.client.RoadShaders",
      "com.sora.splineroads.client.RoadSplitterClient",
      "com.sora.splineroads.client.RoadTerrainHandoff",
      "com.sora.splineroads.client.RoadTerrainModels",
      "com.sora.splineroads.client.RoadTerrainModels$Streaming",
      "com.sora.splineroads.world.RailbedTool",
      "com.sora.splineroads.world.RoadCommands",
      "com.sora.splineroads.world.RoadEvents",
      "com.sora.splineroads.world.RoadWeather"
  };

  private static final Map<Class<?>, List<Method>> HANDLERS = new ConcurrentHashMap<>();
  private static final Set<String> LOADED = ConcurrentHashMap.newKeySet();

  public static synchronized void scan() {
    ClassLoader loader = ForgeEventBus.class.getClassLoader();
    for (String name : SUBSCRIBERS) {
      if (LOADED.contains(name)) continue;
      try {
        Class<?> type = Class.forName(name, false, loader);
        for (Method method : type.getDeclaredMethods()) {
          if (method.getAnnotation(SubscribeEvent.class) == null) continue;
          if (!Modifier.isStatic(method.getModifiers())) continue;
          if (method.getParameterCount() != 1) continue;
          method.setAccessible(true);
          HANDLERS.computeIfAbsent(method.getParameterTypes()[0], k -> new CopyOnWriteArrayList<>()).add(method);
        }
        LOADED.add(name);
      } catch (Throwable absent) {
        // Client-only classes are unavailable on a dedicated server; retry later.
      }
    }
  }

  /** Returns true when the event was cancelled, matching Forge semantics. */
  public static boolean post(Object event) {
    if (event == null) return false;
    scan();
    for (Class<?> type = event.getClass(); type != null && type != Object.class; type = type.getSuperclass()) {
      List<Method> list = HANDLERS.get(type);
      if (list == null) continue;
      for (Method method : list) {
        try {
          method.invoke(null, event);
        } catch (InvocationTargetException failure) {
          System.getLogger("SplineRoads/bridge").log(System.Logger.Level.ERROR,
              "Subscriber " + method + " failed", failure.getCause());
        } catch (Throwable failure) {
          System.getLogger("SplineRoads/bridge").log(System.Logger.Level.ERROR,
              "Subscriber " + method + " failed", failure);
        }
      }
    }
    return event instanceof Event e && e.isCanceled();
  }

  public static void register(Object target) { /* eager registration */ }
  public static int subscriberCount() { return LOADED.size(); }
  private ForgeEventBus() {}
}
