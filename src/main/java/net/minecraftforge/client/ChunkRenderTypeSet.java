package net.minecraftforge.client;

import java.util.*;
import net.minecraft.client.renderer.RenderType;

/** Fabric port: vanilla chunk rebuild only uses the solid layer here. */
public final class ChunkRenderTypeSet {
  private static final ChunkRenderTypeSet NONE = new ChunkRenderTypeSet(Set.of());
  private final Set<RenderType> types;

  private ChunkRenderTypeSet(Set<RenderType> types) { this.types = types; }
  public static ChunkRenderTypeSet of(RenderType... layers) {
    Set<RenderType> set = Collections.newSetFromMap(new IdentityHashMap<>());
    Collections.addAll(set, layers);
    return new ChunkRenderTypeSet(Collections.unmodifiableSet(set));
  }
  public static ChunkRenderTypeSet none() { return NONE; }
  public static ChunkRenderTypeSet union(ChunkRenderTypeSet a, ChunkRenderTypeSet b) {
    Set<RenderType> set = Collections.newSetFromMap(new IdentityHashMap<>());
    set.addAll(a.types); set.addAll(b.types);
    return new ChunkRenderTypeSet(Collections.unmodifiableSet(set));
  }
  public boolean contains(RenderType layer) { return layer == null || types.contains(layer); }
  public boolean isEmpty() { return types.isEmpty(); }
}
