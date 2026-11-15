package net.minecraftforge.fml;

import java.util.Optional;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraftforge.fml.config.IConfigEvent;

/** Fabric port: reports real mods through Fabric Loader, including optional MTR. */
public final class ModList {
  private static final ModList INSTANCE = new ModList();
  public static ModList get() { return INSTANCE; }
  public Optional<ModContainer> getModContainerById(String id) {
    return FabricLoader.getInstance().getModContainer(id).map(ModContainer::new);
  }
  public boolean isLoaded(String id) { return FabricLoader.getInstance().isModLoaded(id); }

  public static final class ModContainer {
    private final net.fabricmc.loader.api.ModContainer container;
    private ModContainer(net.fabricmc.loader.api.ModContainer container) { this.container = container; }
    public ModInfo getModInfo() { return new ModInfo(container); }
    public void dispatchConfigEvent(IConfigEvent event) { /* live config values need no re-dispatch */ }
  }
  public static final class ModInfo {
    private final net.fabricmc.loader.api.ModContainer container;
    private ModInfo(net.fabricmc.loader.api.ModContainer container) { this.container = container; }
    public Object getVersion() { return container.getMetadata().getVersion().getFriendlyString(); }
  }
  private ModList() {}
}
