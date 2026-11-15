package com.sora.splineroads.fabric.mixin;

import com.sora.splineroads.compat.mtr.MtrRailAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Replaces Forge coremod's Data.sync invalidation hook. Optional target. */
@Mixin(targets = "org.mtr.core.data.Data", remap = false)
public abstract class MtrDataMixin {
  @Inject(method = "sync()V", at = @At("RETURN"), require = 0, remap = false)
  private void sr$changed(CallbackInfo info) { MtrRailAccess.changed(this); }
}
