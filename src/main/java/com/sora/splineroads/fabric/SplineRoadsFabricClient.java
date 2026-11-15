package com.sora.splineroads.fabric;

import com.sora.splineroads.config.RoadClientConfig;
import net.fabricmc.api.ClientModInitializer;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.ForgeEventBus;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.network.simple.SimpleChannel;

/** Client entrypoint: client-only subscribers, render hooks and local commands. */
public final class SplineRoadsFabricClient implements ClientModInitializer {
  @Override public void onInitializeClient() {
    SimpleChannel.registerClientReceivers();
    ForgeEventBus.scan();

    ForgeEventBus.post(new FMLClientSetupEvent());
    ForgeEventBus.post(new RegisterKeyMappingsEvent());
    ForgeEventBus.post(new EntityRenderersEvent.RegisterRenderers());
    ForgeEventBus.post(new RegisterColorHandlersEvent.Block());
    ForgeEventBus.post(new ModConfigEvent.Loading(new ModConfig(RoadClientConfig.SPEC)));

    FabricEventsClient.register();
    System.getLogger("SplineRoads").log(System.Logger.Level.INFO,
        "Spline Roads client bridge ready (" + ForgeEventBus.subscriberCount() + " subscriber classes)");
  }
}
