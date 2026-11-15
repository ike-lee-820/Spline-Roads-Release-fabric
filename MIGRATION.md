# Forge → Fabric 迁移说明（Spline Roads 2.1.0）

## 当前状态

✅ **编译已通过**：GitHub Actions（Ubuntu + Temurin JDK 17 + Fabric Loom 1.6）执行 `./gradlew build` 返回 `BUILD SUCCESSFUL`，产出 `splineroads-fabric-2.1.0+fabric.jar`（约 4.3 MB）与 sources jar。
⚠️ **未做游戏内实测**：运行时行为请在测试世界验证。

## 迁移策略

1. **保持原版逻辑层基本不动**：Forge 1.20.1（ForgeGradle 6）与 Fabric Loom 均可使用 Mojang 官方映射（mojmap）。`build.gradle` 里 `mappings loom.officialMojangMappings()` 让 `net.minecraft.*` 的类名、方法名、字段名与 Forge 源码一致，385 个原版 Java 文件中绝大多数无需改动。
2. **兼容层而非全量改写**：原版只有 30 个文件直接引用 Forge API。为避免改动 5.5 万行业务代码，新增 `src/main/java/net/minecraftforge/**` 下的自写适配类（**不是 Forge 的代码**），提供同名 API，由 `com.sora.splineroads.fabric` 下的入口与事件桥驱动。
3. **Forge 对 vanilla 的 patch 需逐项消解**：Forge 给若干原版类「加了」方法或构造（`Entity.getPersistentData()`、`BakedQuad.hasAmbientOcclusion()`、`CreativeModeTab.builder()` 无参版等）。这些在 Fabric 上不存在，已按下表改写或由兼容层吸收。

## 目录结构

```
.github/workflows/build.yml                  CI：JDK17 + Loom，构建并回写日志/jar
build.gradle / settings.gradle / gradle.properties   Fabric Loom 工程
src/main/resources/fabric.mod.json          入口点、依赖、非官方声明
src/main/resources/splineroads.mixins.json  客户端 mixin（setScreen / startUseItem）
src/main/resources/splineroads.accesswidener 预留（当前未启用）
src/main/java/com/sora/splineroads/**       原版代码（少量适配改动）
src/main/java/com/sora/splineroads/fabric/**  Fabric 入口、事件桥、区块跟踪、模型钩子
src/main/java/net/minecraftforge/**          自写 Forge API 兼容层
ci-out/                                     CI 自动回写的 build.log 与 jar
```

## API 对照表

### Forge 框架 API → 兼容层 / Fabric

| Forge 侧 | Fabric 侧实现 |
| --- | --- |
| `@Mod` / `@Mod.EventBusSubscriber` / `@SubscribeEvent` | 保留注解；`ForgeEventBus` 显式扫描 20 个订阅类并分发 |
| `DeferredRegister` / `RegistryObject` | `Registry.register()` 立即注册；`RegistryObject.get()` 直接返回对象 |
| `ForgeRegistries.BLOCKS/ITEMS/BLOCK_ENTITY_TYPES` | `BuiltInRegistries.*` |
| `SimpleChannel` / `NetworkRegistry` / `PacketDistributor` | 反射调用 `ServerPlayNetworking` / `ClientPlayNetworking` 自定义通道 |
| `ForgeConfigSpec` | Gson 读写 `config/splineroads-fabric.json`；`Builder` 公开构造、`SPEC.save()` 空操作（set 即落盘） |
| `Dist` / `DistExecutor` / `ModList` | `FabricLoader.getEnvironmentType()` / 固定返回本 mod 存在、MTR 不存在 |
| `MinecraftForge.EVENT_BUS.post` | `ForgeEventBus.post`，返回是否被取消 |
| `RenderLevelStageEvent` | `WorldRenderEvents.AFTER_ENTITIES`（AFTER_BLOCK_ENTITIES）与 `END`（AFTER_TRANSLUCENT_BLOCKS） |
| `TickEvent.*` | `ServerTickEvents` / `ClientTickEvents` / `WorldRenderEvents.START` |
| `RegisterCommandsEvent` / `RegisterClientCommandsEvent` | `CommandRegistrationCallback` / `ClientCommandRegistrationCallback` |
| `RegisterShadersEvent` | 客户端启动/资源重载后由事件桥触发；`ShaderInstance` 用 vanilla 的 `(ResourceProvider, String, VertexFormat)` 构造，provider 把 `minecraft:shaders/core/road_world_*.json` 重定向到本 mod 命名空间 |
| `ModelEvent.ModifyBakingResult` | `ModelHook` 反射定位 `ModelBakery` 的 baked model map 并 `replaceAll` |
| `EntityRenderersEvent` / `RegisterColorHandlersEvent` / `RegisterKeyMappingsEvent` | 对应 Fabric 注册表（反射调用） |
| `BlockEvent.BreakEvent` | `PlayerBlockBreakEvents.BEFORE` |
| `PlayerInteractEvent.RightClickBlock` | `UseBlockCallback.EVENT` |
| `ChunkWatchEvent.Watch/UnWatch` | `ChunkWatchTracker` 每 5 tick 依据玩家视距合成 |
| `ChunkEvent.Load/Unload` | `ServerChunkEvents` / `ClientChunkEvents` |
| `PlayerEvent.PlayerLoggedOutEvent` | `ServerPlayConnectionEvents.DISCONNECT` |
| `ServerStopping/StoppedEvent` | `ServerLifecycleEvents` |
| `ModelData` / `ModelProperty` / `BakedModelWrapper` / `ChunkRenderTypeSet` | 兼容层内的独立实现；`ForgeBakedModel` 接口承载 Forge 扩展方法，并从 `BlockEntity.getModelData()` 反射取值 |
| `BlockSnapshot` / `BlockEvent.EntityPlaceEvent` | 兼容层内的轻量实现，用于区域保护探测 |
| `ModConfigEvent` / `IConfigEvent` / `FMLClientSetupEvent` | 兼容层内的一次性事件 |

### Forge 注入到 vanilla 类的 API → 本次改写

| 原版写法 | 1.20.1 vanilla / 本移植 |
| --- | --- |
| `player.getPersistentData()`（Forge 注入 `Entity`） | `PersistentData.of(player)`，进程内 `Map<UUID, CompoundTag>` |
| `state.getSoundType(level,pos,player)` | `state.getSoundType()`（无参） |
| `BlockEntity.getRenderBoundingBox()` `@Override` | 去掉 `@Override`，保留普通方法 |
| `BakedQuad.hasAmbientOcclusion()` | 不存在；6 参构造的第 6 个布尔参数统一删除，5 参构造保留 `isShade()` |
| `new BakedQuad(..., boolean, boolean)` | `new BakedQuad(..., boolean)`（5 参） |
| `ModelBlockRenderer.renderModel(..., ModelData, RenderType)`（11 参） | vanilla 9 参版本 |
| `CreativeModeTab.builder()`（无参） | `CreativeModeTab.builder(Row.TOP, 0)` |
| `new ShaderInstance(provider, ResourceLocation, format)` | `new ShaderInstance(provider, "road_world_*", format)` + provider 命名空间重定向 |
| `ResourceLocation.fromNamespaceAndPath(...)` | `new ResourceLocation(namespace, path)` |
| `ForgeConfigSpec.Builder()` 包私有构造 | 改为 `public` |
| `SPEC.save()` | 兼容层提供 public `save()`（set 已自动落盘） |
| `BlockEntity.getModelData()` / `requestModelDataUpdate()` | 去掉 Forge 专有调用；`sendBlockUpdated` 触发区块重绘 |

## 构建与 CI

```bash
./gradlew build
```

`.github/workflows/build.yml` 在 push 到 `main` 或手动 `workflow_dispatch` 时运行：checkout → setup-java 17 → `chmod +x gradlew` → `./gradlew build --stacktrace --no-daemon`，并把日志与 `build/libs/*.jar` 上传为 Actions artifacts，同时回写到仓库 `ci-out/`（回写 commit 带 `[skip ci]`，不会递归触发）。

## 已知降级 / 未完成

- **MTR 铁路路基联动**：关闭（Forge 专属依赖）。
- **Forge coremods**（`terrain_upload.js` / `mtr_snapshot.js`）：未移植。原版针对 `ChunkRenderDispatcher$RenderChunk$RebuildTask` 的 ASM 上传钩子在 Fabric 侧没有等价注入，因此 **TERRAIN 后端不完整**；默认 AUTO 在无 shader pack 时使用 VBO 后端。若需完整 TERRAIN 支持，需补一个针对 Fabric 区块重建的 mixin。
- **ModelData 传递**：vanilla 的 `BakedModel.getQuads(state, side, random)` 不携带 `ModelData`。兼容层在 `getModelData()` 阶段通过反射读取 `BlockEntity.getModelData()`，因此 `RoadFillEntity` 的材质/高度数据仍可进入包装模型；但 vanilla 区块重建不会为每个位置调用 `getModelData()`，与 `RoadTerrainHandoff` 相关的逐 cell 路面四边形（TERRAIN 后端）仍不完整，VBO 后端不受影响。
- **爆炸保护**：`ExplosionEvent.Detonate` 未接入，道路方块可能被爆炸影响。
- **规划器设置持久化**：`PersistentData` 只存活于服务端进程，重启后 `SRPlannerRadius` / `SRPlannerUndo` / `SRPlannerCacheSeconds` 回到默认值。
- **Screen / 输入 mixin**：`require = 0`，若目标方法名与本地映射不符则静默跳过，地图编辑器「关闭子界面返回地图」的行为会降级。
- **自定义 shader**：`RegisterShadersEvent` 在客户端启动/资源重载时尝试注册 3 个 shader；若资源尚不可用则记录错误并回退到 vanilla `rendertypeEntityCutoutNoCull` shader，不影响 VBO 渲染。

## 许可

原版自定义非商业许可要求：保留原作者署名与许可声明、修改版明确标注为非官方、禁止商业用途。本仓库的 `LICENSE`、`THIRD_PARTY_NOTICES.md`、`THIRD_PARTY_LICENSES/`、`fabric.mod.json` 描述与 README 均已满足。
