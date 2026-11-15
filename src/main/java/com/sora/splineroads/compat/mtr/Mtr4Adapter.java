package com.sora.splineroads.compat.mtr;

/**
 * Fabric port stub: MTR 4.0.5 ships for Forge only. All optional hooks are
 * inert; MtrRailAccess.unavailable() reports the dependency as missing.
 */
final class Mtr4Adapter {
  static boolean active() { return false; }
  static boolean trainNode(Object block) { return false; }
  static boolean railbedItem(Object item) { return false; }
  static Object nodeShape(Object original, Object world, Object position) { return original; }
  static void tick(Object simulator) { }
  private Mtr4Adapter() {}
}
