package net.minecraftforge.client.event;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.Event;

public abstract class RenderGuiOverlayEvent extends Event {
  private final GuiGraphics graphics;
  private final Overlay overlay;
  protected RenderGuiOverlayEvent(GuiGraphics graphics, Overlay overlay) {
    this.graphics = graphics; this.overlay = overlay;
  }
  public GuiGraphics getGuiGraphics() { return graphics; }
  public Overlay getOverlay() { return overlay; }

  public static final class Overlay {
    private final ResourceLocation id;
    public Overlay(ResourceLocation id) { this.id = id; }
    public ResourceLocation id() { return id; }
  }

  public static class Post extends RenderGuiOverlayEvent {
    public Post(GuiGraphics graphics, Overlay overlay) { super(graphics, overlay); }
  }
}
