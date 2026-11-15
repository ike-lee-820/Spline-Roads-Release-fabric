package com.sora.splineroads.client;

import com.sora.splineroads.SplineRoads;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

/**
 * Fabric port: config values are live in the shim and command writes take effect
 * immediately, so Forge's NightConfig file-watcher / reload dispatch is omitted.
 */
@Mod.EventBusSubscriber(modid = SplineRoads.ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class RoadClientConfigReload {
  @SubscribeEvent public static void loaded(ModConfigEvent.Loading event) { }
  private RoadClientConfigReload() {}
}
