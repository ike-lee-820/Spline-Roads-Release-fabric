package com.sora.splineroads.fabric.mixin;

import com.sora.splineroads.fabric.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.ForgeEventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Supplies the two client events Fabric API does not expose directly. */
@Mixin(Minecraft.class)
public abstract class MinecraftEventsMixin {
  @Inject(method = "setScreen(Lnet/minecraft/client/gui/screens/Screen;)V",
      at = @At("HEAD"), cancellable = true, require = 0)
  private void sr$opening(Screen requested, CallbackInfo info) {
    if (ScreenEvents.redirecting()) return;
    Minecraft mc = Minecraft.getInstance();
    ScreenEvent.Opening event = new ScreenEvent.Opening(mc.screen, requested);
    ForgeEventBus.post(event);
    Screen replacement = event.getNewScreen();
    if (replacement != requested) {
      ScreenEvents.redirect(() -> mc.setScreen(replacement));
      info.cancel();
    }
  }

  @Inject(method = "startUseItem()V", at = @At("HEAD"), cancellable = true, require = 0)
  private void sr$useItem(CallbackInfo info) {
    Minecraft mc = Minecraft.getInstance();
    Player player = mc.player;
    if (player == null) return;
    ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
    InputEvent.InteractionKeyMappingTriggered event =
        new InputEvent.InteractionKeyMappingTriggered(true, InteractionHand.MAIN_HAND);
    ForgeEventBus.post(event);
    if (event.isCanceled()) info.cancel();
  }
}
