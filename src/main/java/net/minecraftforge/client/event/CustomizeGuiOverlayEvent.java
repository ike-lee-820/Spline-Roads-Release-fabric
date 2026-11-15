package net.minecraftforge.client.event;

import java.util.ArrayList;
import java.util.List;
import net.minecraftforge.eventbus.api.Event;

public abstract class CustomizeGuiOverlayEvent extends Event {
  private final List<String> left = new ArrayList<>();
  private final List<String> right = new ArrayList<>();
  public List<String> getLeft() { return left; }
  public List<String> getRight() { return right; }

  public static final class DebugText extends CustomizeGuiOverlayEvent {}
}
