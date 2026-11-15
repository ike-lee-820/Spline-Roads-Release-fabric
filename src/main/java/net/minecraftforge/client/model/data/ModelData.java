package net.minecraftforge.client.model.data;

import java.util.*;

/** Fabric port: immutable property map, API-compatible with Forge ModelData. */
public final class ModelData {
  public static final ModelData EMPTY = new ModelData(new HashMap<>());
  private final Map<ModelProperty<?>, Object> values;

  private ModelData(Map<ModelProperty<?>, Object> values) {
    this.values = Collections.unmodifiableMap(new HashMap<>(values));
  }

  public static Builder builder() { return new Builder(new HashMap<>()); }
  public Builder derive() { return new Builder(new HashMap<>(values)); }

  /** Values from {@code override} win over {@code base}. */
  public static ModelData merge(ModelData base, ModelData override) {
    if (base == null || base == EMPTY) return override == null ? EMPTY : override;
    if (override == null || override == EMPTY) return base;
    Map<ModelProperty<?>, Object> joined = new HashMap<>(base.values);
    joined.putAll(override.values);
    return new ModelData(joined);
  }

  @SuppressWarnings("unchecked")
  public <T> T get(ModelProperty<T> property) { return (T) values.get(property); }
  public boolean has(ModelProperty<?> property) { return values.containsKey(property); }

  public static final class Builder {
    private final Map<ModelProperty<?>, Object> values;
    private Builder(Map<ModelProperty<?>, Object> values) { this.values = values; }
    public <T> Builder with(ModelProperty<T> property, T value) {
      if (value != null) values.put(property, value);
      return this;
    }
    public ModelData build() { return new ModelData(values); }
  }
}
