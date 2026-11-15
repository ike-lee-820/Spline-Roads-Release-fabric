package net.minecraftforge.client.event;

import java.util.Map;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.Event;

public abstract class ModelEvent extends Event {
  public static final class ModifyBakingResult extends ModelEvent {
    private final Map<ResourceLocation, BakedModel> models;
    public ModifyBakingResult(Map<ResourceLocation, BakedModel> models) { this.models = models; }
    public Map<ResourceLocation, BakedModel> getModels() { return models; }
  }
  public static final class BakingCompleted extends ModelEvent {
    private final ModelManager manager;
    public BakingCompleted(ModelManager manager) { this.manager = manager; }
    public ModelManager getModelManager() { return manager; }
  }
}
