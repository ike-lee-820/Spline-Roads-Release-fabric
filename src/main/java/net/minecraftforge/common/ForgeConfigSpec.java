package net.minecraftforge.common;

import com.google.gson.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fabric port of the small ForgeConfigSpec surface used by Spline Roads.
 * Values live in config/splineroads-fabric.json (Gson, bundled with Minecraft)
 * and are read on every get(), so command changes take effect immediately.
 */
public final class ForgeConfigSpec {
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final Path FILE = Paths.get("config", "splineroads-fabric.json");
  private static final Map<String, String> STORE = new ConcurrentHashMap<>();

  static { reload(); }

  public static void reload() {
    try {
      if (!Files.exists(FILE)) return;
      JsonObject object = JsonParser.parseString(new String(Files.readAllBytes(FILE), StandardCharsets.UTF_8)).getAsJsonObject();
      for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
        if (entry.getValue().isJsonPrimitive()) STORE.put(entry.getKey(), entry.getValue().getAsString());
      }
    } catch (Throwable failure) {
      System.getLogger("SplineRoads/config").log(System.Logger.Level.WARNING, "Config read failed", failure);
    }
  }

  private static void save() {
    try {
      Files.createDirectories(FILE.getParent());
      JsonObject object = new JsonObject();
      new TreeMap<>(STORE).forEach(object::addProperty);
      Files.write(FILE, GSON.toJson(object).getBytes(StandardCharsets.UTF_8));
    } catch (Throwable failure) {
      System.getLogger("SplineRoads/config").log(System.Logger.Level.WARNING, "Config write failed", failure);
    }
  }

  public static abstract class Base<T> {
    protected final String key;
    protected final T fallback;
    protected Base(String key, T fallback) { this.key = key; this.fallback = fallback; STORE.putIfAbsent(key, String.valueOf(fallback)); }
    protected String raw() { return STORE.getOrDefault(key, String.valueOf(fallback)); }
    public abstract T get();
    public void set(T value) { STORE.put(key, String.valueOf(value)); save(); }
  }

  public static final class BooleanValue extends Base<Boolean> {
    BooleanValue(String key, boolean fallback) { super(key, fallback); }
    @Override public Boolean get() { return Boolean.parseBoolean(raw()); }
    public void set(boolean value) { set(Boolean.valueOf(value)); }
  }

  public static final class IntValue extends Base<Integer> {
    private final int min, max;
    IntValue(String key, int fallback, int min, int max) { super(key, fallback); this.min = min; this.max = max; }
    @Override public Integer get() {
      try { return Math.max(min, Math.min(max, Integer.parseInt(raw()))); }
      catch (NumberFormatException bad) { return fallback; }
    }
    public void set(int value) { set(Integer.valueOf(value)); }
  }

  public static final class EnumValue<E extends Enum<E>> extends Base<E> {
    private final Class<E> type;
    EnumValue(String key, E fallback) { super(key, fallback); this.type = fallback.getDeclaringClass(); }
    @Override public E get() {
      try { return Enum.valueOf(type, raw()); }
      catch (IllegalArgumentException bad) { return fallback; }
    }
  }

  public static final class Builder {
    private final ArrayDeque<String> stack = new ArrayDeque<>();
    Builder() {}
    public Builder push(String name) { stack.push(name); return this; }
    public Builder pop() { if (!stack.isEmpty()) stack.pop(); return this; }
    public Builder comment(String... ignored) { return this; }
    private String path(String name) {
      if (stack.isEmpty()) return name;
      StringBuilder text = new StringBuilder();
      for (Iterator<String> it = stack.descendingIterator(); it.hasNext(); ) text.append(it.next()).append('.');
      return text.append(name).toString();
    }
    public BooleanValue define(String name, boolean fallback) { return new BooleanValue(path(name), fallback); }
    public IntValue defineInRange(String name, int fallback, int min, int max) { return new IntValue(path(name), fallback, min, max); }
    public <E extends Enum<E>> EnumValue<E> defineEnum(String name, E fallback) { return new EnumValue<>(path(name), fallback); }
    public ForgeConfigSpec build() { return new ForgeConfigSpec(); }
  }

  private ForgeConfigSpec() {}
}
