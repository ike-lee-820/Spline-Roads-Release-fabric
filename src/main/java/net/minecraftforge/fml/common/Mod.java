package net.minecraftforge.fml.common;

import java.lang.annotation.*;
import net.minecraftforge.api.distmarker.Dist;

/** Fabric port: annotation retained so subscriber classes stay untouched. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Mod {
  String value();

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  @interface EventBusSubscriber {
    String modid() default "";
    Bus bus() default Bus.FORGE;
    Dist[] value() default {Dist.CLIENT, Dist.DEDICATED_SERVER};
    enum Bus { MOD, FORGE }
  }
}
