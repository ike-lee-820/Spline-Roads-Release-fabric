package com.sora.splineroads.compat.mtr;

import java.lang.reflect.*;
import java.util.*;

/**
 * Reflective gateway to MTR 4.0.5 (Fabric build). Keeps the mod free of a
 * hard compile/runtime dependency: when MTR is absent every lookup fails and
 * the integration quietly stays disabled. Names come from the upstream MTR
 * adapter; lookups are by name so loader-specific mapping differences do not
 * break field access.
 */
final class MtrReflection {
  static final MtrReflection INSTANCE = build();

  final Class<?> simulator, data, rail, position, transportMode, blockNode;
  final Class<?> holderItem, holderBlockView, holderBlockPos, holderVoxelShape;
  final Object train;
  final Field simDimension, simPositionsToRail, simRailIdMap, simRails;
  final Field railMath, mathMinX, mathMaxX, mathMinY, mathMaxY, mathMinZ, mathMaxZ;
  final Field curveX, curveY, curveZ;
  final Field itemData, viewData, posData, shapeData, nodeTransportMode;
  final Method railMode, railRemote, railValid, railHexId, railStyles;
  final Method mathLength, mathPosition;
  final Method posX, posY, posZ;
  final Constructor<?> positionCtor, shapeCtor;

  private MtrReflection() throws Throwable {
    ClassLoader loader = MtrReflection.class.getClassLoader();
    simulator = Class.forName("org.mtr.core.simulation.Simulator", false, loader);
    data = Class.forName("org.mtr.core.data.Data", false, loader);
    rail = Class.forName("org.mtr.core.data.Rail", false, loader);
    position = Class.forName("org.mtr.core.data.Position", false, loader);
    transportMode = Class.forName("org.mtr.core.data.TransportMode", false, loader);
    blockNode = Class.forName("org.mtr.mod.block.BlockNode", false, loader);
    holderItem = Class.forName("org.mtr.mapping.holder.Item", false, loader);
    holderBlockView = Class.forName("org.mtr.mapping.holder.BlockView", false, loader);
    holderBlockPos = Class.forName("org.mtr.mapping.holder.BlockPos", false, loader);
    holderVoxelShape = Class.forName("org.mtr.mapping.holder.VoxelShape", false, loader);
    train = transportMode.getField("TRAIN").get(null);

    simDimension = field(simulator, "dimension");
    simPositionsToRail = field(simulator, "positionsToRail");
    simRailIdMap = field(simulator, "railIdMap");
    simRails = field(simulator, "rails");
    railMath = field(rail, "railMath");
    Class<?> mathType = railMath == null ? null : railMath.getType();
    mathMinX = mathType == null ? null : field(mathType, "minX");
    mathMaxX = mathType == null ? null : field(mathType, "maxX");
    mathMinY = mathType == null ? null : field(mathType, "minY");
    mathMaxY = mathType == null ? null : field(mathType, "maxY");
    mathMinZ = mathType == null ? null : field(mathType, "minZ");
    mathMaxZ = mathType == null ? null : field(mathType, "maxZ");
    mathLength = mathType == null ? null : method(mathType, "getLength", 0);
    mathPosition = mathType == null ? null : method(mathType, "getPosition", 2);
    Class<?> curveType = mathPosition == null ? null : mathPosition.getReturnType();
    curveX = curveType == null ? null : field(curveType, "x");
    curveY = curveType == null ? null : field(curveType, "y");
    curveZ = curveType == null ? null : field(curveType, "z");

    railMode = method(rail, "getTransportMode", 0);
    railRemote = method(rail, "canConnectRemotely", 0);
    railValid = method(rail, "isValid", 0);
    railHexId = method(rail, "getHexId", 0);
    railStyles = method(rail, "getStyles", 0);
    posX = method(position, "getX", 0);
    posY = method(position, "getY", 0);
    posZ = method(position, "getZ", 0);
    itemData = field(holderItem, "data");
    viewData = field(holderBlockView, "data");
    posData = field(holderBlockPos, "data");
    shapeData = field(holderVoxelShape, "data");
    nodeTransportMode = field(blockNode, "transportMode");
    positionCtor = numericCtor(position);
    shapeCtor = oneArgCtor(holderVoxelShape);
    if (simDimension == null || simPositionsToRail == null || simRailIdMap == null
        || railMath == null || mathLength == null || mathPosition == null
        || railMode == null || railHexId == null || positionCtor == null
        || itemData == null || viewData == null || posData == null) {
      throw new IllegalStateException("MTR 4.0.5 API shape mismatch");
    }
  }

  private static MtrReflection build() {
    try { return new MtrReflection(); }
    catch (Throwable absent) { return null; }
  }

  static Object get(Field f, Object target) {
    try { return f == null || target == null ? null : f.get(target); }
    catch (Throwable absent) { return null; }
  }
  static Object call(Method m, Object target, Object... args) {
    try { return m == null ? null : m.invoke(target, args); }
    catch (InvocationTargetException failure) { throw asRuntime(failure.getCause()); }
    catch (Throwable failure) { throw asRuntime(failure); }
  }
  private static RuntimeException asRuntime(Throwable failure) {
    return failure instanceof RuntimeException runtime ? runtime : new IllegalStateException(failure);
  }
  static double number(Object value) { return value instanceof Number n ? n.doubleValue() : 0; }
  static boolean flag(Object value) { return Boolean.TRUE.equals(value); }

  Object newPosition(int x, int y, int z) {
    if (positionCtor == null) return null;
    try {
      Class<?>[] types = positionCtor.getParameterTypes();
      int[] values = {x, y, z};
      Object[] args = new Object[3];
      for (int i = 0; i < 3; i++) {
        Class<?> t = types[i];
        args[i] = t == long.class || t == Long.class ? (long) values[i]
            : t == double.class || t == Double.class ? (double) values[i]
            : t == float.class || t == Float.class ? (float) values[i] : values[i];
      }
      return positionCtor.newInstance(args);
    } catch (Throwable failure) { throw asRuntime(failure); }
  }
  Object newVoxelShape(Object vanilla) {
    try { return shapeCtor == null ? null : shapeCtor.newInstance(vanilla); }
    catch (Throwable failure) { throw asRuntime(failure); }
  }
  int intX(Object pos) { return (int) Math.round(number(call(posX, pos))); }
  int intY(Object pos) { return (int) Math.round(number(call(posY, pos))); }
  int intZ(Object pos) { return (int) Math.round(number(call(posZ, pos))); }
  Object math(Object railObject) { return get(railMath, railObject); }
  double length(Object railObject) { return number(call(mathLength, math(railObject))); }
  Object point(Object railObject, double s, boolean reverse) {
    return call(mathPosition, math(railObject), s, reverse);
  }
  @SuppressWarnings("unchecked")
  Map<Object, Object> neighbours(Object simulatorObject, Object pos) {
    Object all = get(simPositionsToRail, simulatorObject);
    if (!(all instanceof Map<?, ?> map)) return Map.of();
    Object found = map.get(pos);
    return found instanceof Map<?, ?> inner ? (Map<Object, Object>) inner : Map.of();
  }
  @SuppressWarnings("unchecked")
  Map<Object, Object> railById(Object simulatorObject) {
    Object map = get(simRailIdMap, simulatorObject);
    return map instanceof Map<?, ?> found ? (Map<Object, Object>) found : Map.of();
  }
  @SuppressWarnings("unchecked")
  Collection<Object> rails(Object simulatorObject) {
    Object collection = get(simRails, simulatorObject);
    return collection instanceof Collection<?> found ? (Collection<Object>) found : List.of();
  }
  String dimension(Object simulatorObject) { return String.valueOf(get(simDimension, simulatorObject)); }
  boolean trainRail(Object railObject) {
    return railObject != null && get(railMode, railObject) == train
        && !flag(call(railRemote, railObject)) && flag(call(railValid, railObject));
  }
  String hexId(Object railObject) { return String.valueOf(call(railHexId, railObject)); }
  @SuppressWarnings("unchecked")
  List<String> styles(Object railObject) {
    Object value = call(railStyles, railObject);
    return value instanceof List<?> found ? (List<String>) found : List.of();
  }
  static boolean compare(Object a, Object b) {
    return a instanceof Comparable<?> c && ((Comparable<Object>) c).compareTo(b) > 0;
  }

  private static Field field(Class<?> owner, String name) {
    for (Class<?> type = owner; type != null && type != Object.class; type = type.getSuperclass()) {
      try { Field f = type.getDeclaredField(name); f.setAccessible(true); return f; }
      catch (NoSuchFieldException ignored) { }
    }
    return null;
  }
  private static Method method(Class<?> owner, String name, int arity) {
    for (Method m : owner.getMethods()) {
      if (m.getName().equals(name) && m.getParameterCount() == arity) { m.setAccessible(true); return m; }
    }
    return null;
  }
  private static Constructor<?> numericCtor(Class<?> owner) {
    for (Constructor<?> c : owner.getConstructors()) {
      Class<?>[] p = c.getParameterTypes();
      if (p.length != 3) continue;
      boolean numeric = true;
      for (Class<?> t : p) numeric &= t.isPrimitive() && t != boolean.class && t != char.class
          || t == Long.class || t == Integer.class || t == Double.class || t == Float.class;
      if (numeric) { c.setAccessible(true); return c; }
    }
    return null;
  }
  private static Constructor<?> oneArgCtor(Class<?> owner) {
    for (Constructor<?> c : owner.getConstructors()) {
      if (c.getParameterCount() == 1) { c.setAccessible(true); return c; }
    }
    return null;
  }
}
