package net.minecraftforge.registries;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ForgeRegistries {
  public static final Registry<Block> BLOCKS = BuiltInRegistries.BLOCK;
  public static final Registry<Item> ITEMS = BuiltInRegistries.ITEM;
  @SuppressWarnings("unchecked")
  public static final Registry<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
      (Registry<BlockEntityType<?>>) (Registry<?>) BuiltInRegistries.BLOCK_ENTITY_TYPE;
  private ForgeRegistries() {}
}
