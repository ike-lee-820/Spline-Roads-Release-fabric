package net.minecraftforge.eventbus.api;

/** Minimal cancelable event carrier used by the Fabric bridge. */
public abstract class Event {
  private boolean canceled;
  public boolean isCancelable() { return true; }
  public boolean isCanceled() { return canceled; }
  public void setCanceled(boolean value) { this.canceled = value; }
}
