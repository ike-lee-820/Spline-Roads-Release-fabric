package com.sora.splineroads.fabric.mixin;

import com.sora.splineroads.compat.mtr.MtrRailAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Replaces Forge coremod's Simulator.tick snapshot hook. Optional target. */
@Mixin(targets = "org.mtr.core.simulation.Simulator", remap = false)
public abstract class MtrSimulatorMixin {
  @Inject(method = "tick()V", at = @At("RETURN"), require = 0, remap = false)
  private void sr$snapshot(CallbackInfo info) { MtrRailAccess.tick(this); }
}
