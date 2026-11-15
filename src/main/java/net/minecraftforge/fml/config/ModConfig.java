package net.minecraftforge.fml.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class ModConfig {
  public enum Type { CLIENT, COMMON, SERVER, STARTUP }
  private final ForgeConfigSpec spec;
  public ModConfig(ForgeConfigSpec spec) { this.spec = spec; }
  public ForgeConfigSpec getSpec() { return spec; }
  public Object getConfigData() { return spec; }
}
