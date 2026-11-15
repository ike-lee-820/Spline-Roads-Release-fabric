package com.sora.splineroads.fabric;

/** Re-entrancy guard for the setScreen redirect. */
public final class ScreenEvents {
  private static final ThreadLocal<Boolean> ACTIVE = ThreadLocal.withInitial(() -> Boolean.FALSE);
  public static boolean redirecting() { return ACTIVE.get(); }
  public static void redirect(Runnable action) {
    ACTIVE.set(Boolean.TRUE);
    try { action.run(); }
    finally { ACTIVE.set(Boolean.FALSE); }
  }
  private ScreenEvents() {}
}
