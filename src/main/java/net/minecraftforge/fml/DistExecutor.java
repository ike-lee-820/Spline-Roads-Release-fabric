package net.minecraftforge.fml;

import java.util.function.Supplier;
import net.minecraftforge.api.distmarker.Dist;

/** Fabric port: defers client-only work without loading client classes on a server. */
public final class DistExecutor {
  private static volatile boolean client = probe();

  private static boolean probe() {
    try {
      Class<?> loader = Class.forName("net.fabricmc.loader.api.FabricLoader");
      Object instance = loader.getMethod("getInstance").invoke(null);
      Object env = loader.getMethod("getEnvironmentType").invoke(instance);
      return String.valueOf(env).contains("CLIENT");
    } catch (Throwable ignored) {
      try { Class.forName("net.minecraft.client.Minecraft"); return true; }
      catch (Throwable absent) { return false; }
    }
  }

  public static boolean isClient() { return client; }

  public static void unsafeRunWhenOn(Dist dist, Supplier<Runnable> action) {
    boolean wanted = (dist == Dist.CLIENT && client) || (dist == Dist.DEDICATED_SERVER && !client);
    if (wanted) {
      Runnable runnable = action.get();
      if (runnable != null) runnable.run();
    }
  }
  private DistExecutor() {}
}
