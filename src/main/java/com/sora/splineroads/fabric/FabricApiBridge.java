package com.sora.splineroads.fabric;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.InteractionResult;

/** Reflective helper for optional Fabric API events and argument extraction. */
public final class FabricApiBridge {
  public interface Callback { Object invoke(Object[] args); }
  private static final String LOG = "SplineRoads/bridge";

  public static void on(String className, String fieldName, Callback callback) {
    try {
      Class<?> owner = Class.forName(className);
      Object event = owner.getField(fieldName).get(null);
      if (event == null) return;
      Method register = null;
      for (Method m : event.getClass().getMethods()) {
        if (m.getName().equals("register") && m.getParameterCount() == 1) { register = m; break; }
      }
      if (register == null) return;
      Class<?> listener = listenerType(event, register);
      if (listener == null) {
        System.getLogger(LOG).log(System.Logger.Level.WARNING,
            "Cannot resolve listener type for " + className + "." + fieldName);
        return;
      }
      Object proxy = Proxy.newProxyInstance(listener.getClassLoader(),
          new Class<?>[]{listener}, (p, method, args) -> {
            if (method.getDeclaringClass() == Object.class) {
              return switch (method.getName()) {
                case "hashCode" -> System.identityHashCode(p);
                case "equals" -> p == args[0];
                default -> "SplineRoads callback";
              };
            }
            Object result = callback.invoke(args == null ? new Object[0] : args);
            return coerce(result, method.getReturnType());
          });
      register.invoke(event, proxy);
    } catch (Throwable failure) {
      System.getLogger(LOG).log(System.Logger.Level.WARNING,
          "Optional event unavailable: " + className + "." + fieldName + " (" + failure + ")");
    }
  }

  private static Class<?> listenerType(Object event, Method register) {
    List<Type> candidates = new ArrayList<>();
    for (Type t : register.getGenericParameterTypes()) candidates.add(t);
    Type superClass = event.getClass().getGenericSuperclass();
    if (superClass != null) candidates.add(superClass);
    for (Type t : event.getClass().getGenericInterfaces()) candidates.add(t);
    for (Type type : candidates) {
      if (type instanceof Class<?> c && c.isInterface() && c != Object.class) return c;
      if (type instanceof ParameterizedType pt) {
        for (Type arg : pt.getActualTypeArguments()) {
          if (arg instanceof Class<?> c && c.isInterface()) return c;
        }
      }
    }
    for (Field f : event.getClass().getDeclaredFields()) {
      try {
        f.setAccessible(true);
        Object value = f.get(event);
        if (value instanceof Class<?> c && c.isInterface()) return c;
        if (value != null && value.getClass().isArray()) {
          Class<?> component = value.getClass().getComponentType();
          if (component.isInterface()) return component;
        }
      } catch (Throwable ignored) { }
    }
    return null;
  }

  private static Object coerce(Object value, Class<?> type) {
    if (type == void.class || type == Void.class) return null;
    if (type == boolean.class || type == Boolean.class)
      return value instanceof Boolean b ? b : Boolean.TRUE;
    if (value != null && (type.isInstance(value) || type == Object.class)) return value;
    if (value == null) {
      if (type == InteractionResult.class) return InteractionResult.PASS;
      for (String name : new String[]{"PASS", "SUCCESS", "CONTINUE"}) {
        try { Field f = type.getField(name); if (type.isAssignableFrom(f.getType())) return f.get(null); }
        catch (Throwable ignored) { }
      }
    }
    if (type == int.class) return 0;
    if (type == long.class) return 0L;
    if (type == float.class) return 0f;
    if (type == double.class) return 0d;
    return null;
  }

  @SuppressWarnings("unchecked")
  public static <T> T arg(Class<T> type, Object[] args) {
    if (args != null) for (Object value : args) if (type.isInstance(value)) return (T) value;
    return null;
  }

  public static Object call(Object target, String... names) {
    if (target == null) return null;
    for (String name : names) {
      try {
        for (Method m : target.getClass().getMethods()) {
          if (m.getName().equals(name) && m.getParameterCount() == 0) return m.invoke(target);
        }
      } catch (Throwable ignored) { }
    }
    return null;
  }

  public static Object field(Object target, Class<?> type) {
    if (target == null) return null;
    for (Class<?> current = target.getClass(); current != null && current != Object.class; current = current.getSuperclass()) {
      for (Field f : current.getDeclaredFields()) {
        if (type.isAssignableFrom(f.getType())) {
          try { f.setAccessible(true); return f.get(target); }
          catch (Throwable ignored) { }
        }
      }
    }
    return null;
  }

  private FabricApiBridge() {}
}
