package com.sora.splineroads.fabric.mixin;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.ForgeEventBus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Replaces Forge's ExplosionEvent.Detonate post in Explosion.finalizeExplosion. */
@Mixin(Explosion.class)
public abstract class ExplosionMixin {
  @Shadow @Final private Level level;
  @Shadow @Final private ObjectArrayList<BlockPos> toBlow;

  @Inject(method = "finalizeExplosion(Z)V", at = @At("HEAD"))
  private void sr$detonate(boolean spawnParticles, CallbackInfo info) {
    if (level == null || toBlow == null || toBlow.isEmpty()) return;
    ForgeEventBus.post(new ExplosionEvent.Detonate(level, toBlow));
  }
}
