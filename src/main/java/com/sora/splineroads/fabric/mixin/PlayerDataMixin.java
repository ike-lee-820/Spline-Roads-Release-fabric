package com.sora.splineroads.fabric.mixin;

import com.sora.splineroads.fabric.PersistentData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Persists planner settings in the player's own NBT, like Forge's ForgeData. */
@Mixin(Player.class)
public abstract class PlayerDataMixin {
  private static final String SR_KEY = "SplineRoadsPlanner";

  @Inject(method = "addAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("TAIL"))
  private void sr$save(CompoundTag tag, CallbackInfo info) {
    Player self = (Player) (Object) this;
    if (!(self instanceof ServerPlayer)) return;
    CompoundTag data = PersistentData.snapshot(self.getUUID());
    if (data != null) tag.put(SR_KEY, data);
  }

  @Inject(method = "readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("TAIL"))
  private void sr$load(CompoundTag tag, CallbackInfo info) {
    Player self = (Player) (Object) this;
    if (tag.contains(SR_KEY, 10)) PersistentData.restore(self.getUUID(), tag.getCompound(SR_KEY));
  }
}
