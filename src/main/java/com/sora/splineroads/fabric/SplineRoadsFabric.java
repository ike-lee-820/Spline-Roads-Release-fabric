package com.sora.splineroads.fabric;

import com.sora.splineroads.SplineRoads;
import net.fabricmc.api.ModInitializer;
import net.minecraftforge.eventbus.api.ForgeEventBus;

/** Common entrypoint: registration, configuration, networking and server events. */
public final class SplineRoadsFabric implements ModInitializer {
  @Override public void onInitialize() {
    new SplineRoads();
    ForgeEventBus.scan();
    FabricEventsCommon.register();
    System.getLogger("SplineRoads").log(System.Logger.Level.INFO,
        "Spline Roads Fabric port initialized (" + ForgeEventBus.subscriberCount() + " subscriber classes)");
  }
}
