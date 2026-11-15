package net.minecraftforge.client.model;

import java.util.List;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

/** Adapts a plain vanilla BakedModel to the Forge-style extension interface. */
public final class ForgeBakedModels {
  public static ForgeBakedModel of(BakedModel model) {
    if (model instanceof ForgeBakedModel extended) return extended;
    return new Plain(model);
  }

  private static final class Plain implements ForgeBakedModel {
    private final BakedModel delegate;
    private Plain(BakedModel delegate) { this.delegate = delegate; }
    public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource random) {
      return delegate.getQuads(state, side, random);
    }
    public boolean useAmbientOcclusion() { return delegate.useAmbientOcclusion(); }
    public boolean isGui3d() { return delegate.isGui3d(); }
    public boolean usesBlockLight() { return delegate.usesBlockLight(); }
    public boolean isCustomRenderer() { return delegate.isCustomRenderer(); }
    public TextureAtlasSprite getParticleIcon() { return delegate.getParticleIcon(); }
    public net.minecraft.client.resources.model.ItemTransforms getTransforms() {
      return delegate.getTransforms();
    }
    public net.minecraft.client.renderer.block.model.ItemOverrides getOverrides() {
      return delegate.getOverrides();
    }
  }
  private ForgeBakedModels() {}
}
