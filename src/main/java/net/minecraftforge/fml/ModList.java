package net.minecraftforge.fml;

import java.util.Optional;
import net.minecraftforge.fml.config.IConfigEvent;

/** Fabric port: only the built-in mod is advertised; optional MTR stays absent. */
public final class ModList {
  private static final ModList INSTANCE = new ModList();
  private static final ModContainer SELF = new ModContainer();

  public static ModList get() { return INSTANCE; }
  public Optional<ModContainer> getModContainerById(String id) {
    return "mtr".equals(id) ? Optional.empty() : Optional.of(SELF);
  }
  public boolean isLoaded(String id) { return !"mtr".equals(id); }

  public static final class ModContainer {
    private final ModInfo info = new ModInfo();
    public ModInfo getModInfo() { return info; }
    public void dispatchConfigEvent(IConfigEvent event) { /* live config values need no re-dispatch */ }
  }
  public static final class ModInfo {
    public Object getVersion() { return "2.1.0+fabric"; }
  }
  private ModList() {}
}
