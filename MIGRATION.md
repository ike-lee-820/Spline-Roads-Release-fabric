# Forge → Fabric 迁移说明（Spline Roads 2.1.0）

## 迁移策略

1. **保持原版逻辑层不动**：Forge 1.20.1（ForgeGradle 6）与 Fabric Loom 均可使用 Mojang 官方映射（mojmap）。`build.gradle` 里 `mappings loom.officialMojangMappings()` 让 `net.minecraft.*` 的类名、方法名、字段名与 Forge 源码一致，385 个原版 Java 文件中约 355 个（core / world / 大部分 client 逻辑）无需任何改动。
2. **兼容层而非全量改写**：原版只有 30 个文件直接引用 Forge API。为避免改动 5.5 万行业务代码，新增 `src/main/java/net/minecraftforge/**` 下的自写适配类（**不是 Forge 的代码**），提供同名 API，由 `com.sora.splineroads.fabric` 下的入口与事件桥驱动。
3. **必要的少量源码改写**（共 7 处）：`ResourceLocation.fromNamespaceAndPath` → 构造器；`RoadTerrainModels.markerModel` 返回扩展接口；`RoadFabricModels` 的 `getModelData` 调用；`RoadFillEntity` 的 Forge 专有 `getModelData()` / `requestModelDataUpdate()`；`RoadClientConfigReload` 移除 NightConfig；MTR 两个适配类改为惰性存根。

## 目录结构

```
build.gradle / settings.gradle / gradle.properties   Fabric Loom 工程
src/main/resources/fabric.mod.json                  入口点、依赖、非官方声明
src/main/resources/splineroads.mixins.json         客户端 mixin（setScreen / startUseItem）
src/main/resources/splineroads.accesswidener       预留（当前未启用）
src/main/java/com/sora/splineroads/**              原版代码（除上述 7 处）
src/main/java/com/sora/splineroads/fabric/**       Fabric 入口、事件桥、区块跟踪、模型钩子
src/main/java/net/minecraftforge/**                 自写 Forge API 兼容层
```

## API 对照表

| Forge 侧 | Fabric 侧实现 |
| --- | --- |
| `@Mod` / `@Mod.EventBusSubscriber` / `@SubscribeEvent` | 保留注解；`ForgeEventBus` 显式扫描 20 个订阅类并分发 |
| `DeferredRegister` / `RegistryObject` | `Registry.register()` 立即注册；`RegistryObject.get()` 直接返回对象 |
| `ForgeRegistries.BLOCKS/ITEMS/BLOCK_ENTITY_TYPES` | `BuiltInRegistries.*` |
| `SimpleChannel` / `NetworkRegistry` / `PacketDistributor` | 反射调用 `ServerPlayNetworking` / `ClientPlayNetworking` 自定义通道 |
| `ForgeConfigSpec` | Gson 读写 `config/splineroads-fabric.json` |
| `Dist` / `DistExecutor` / `ModList` | `FabricLoader.getEnvironmentType()` / 固定返回本 mod 存在、MTR 不存在 |
| `MinecraftForge.EVENT_BUS.post` | `ForgeEventBus.post`，返回是否被取消 |
| `RenderLevelStageEvent` | `WorldRenderEvents.AFTER_ENTITIES`（AFTER_BLOCK_ENTITIES）与 `END`（AFTER_TRANSLUCENT_BLOCKS） |
| `TickEvent.*` | `ServerTickEvents` / `ClientTickEvents` / `WorldRenderEvents.START` |
| `RegisterCommandsEvent` / `RegisterClientCommandsEvent` | `CommandRegistrationCallback` / `ClientCommandRegistrationCallback` |
| `RegisterShadersEvent` | 客户端启动/资源重载后由事件桥触发，`ShaderInstance` 由模组自行构造 |
| `ModelEvent.ModifyBakingResult` | `ModelHook` 反射定位 `ModelBakery` 的 baked model map 并 `replaceAll` |
| `EntityRenderersEvent` / `RegisterColorHandlersEvent` / `RegisterKeyMappingsEvent` | 对应 Fabric 注册表（反射调用） |
| `BlockEvent.BreakEvent` | `PlayerBlockBreakEvents.BEFORE` |
| `PlayerInteractEvent.RightClickBlock` | `UseBlockCallback.EVENT` |
| `ChunkWatchEvent.Watch/UnWatch` | `ChunkWatchTracker` 每 5 tick 依据玩家视距合成 |
| `ChunkEvent.Load/Unload` | `ServerChunkEvents` / `ClientChunkEvents` |
| `PlayerEvent.PlayerLoggedOutEvent` | `ServerPlayConnectionEvents.DISCONNECT` |
| `ServerStopping/StoppedEvent` | `ServerLifecycleEvents` |
| `ModelData` / `ModelProperty` / `BakedModelWrapper` / `ChunkRenderTypeSet` | 兼容层内的独立实现；`ForgeBakedModel` 接口承载 Forge 扩展方法 |
| `BlockSnapshot` / `BlockEvent.EntityPlaceEvent` | 兼容层内的轻量实现，用于区域保护探测 |
| `ModConfigEvent` / `IConfigEvent` / `FMLClientSetupEvent` | 兼容层内的一次性事件 |

## 需本地确认（首次构建时）

自动迁移环境无法访问 Maven / Mojang 仓库，因此**未执行 Gradle 编译验证**。以下几点请按顺序核对：

1. **Fabric API 版本签名**：`FabricEventsCommon` / `FabricEventsClient` 直接引用了 1.20.1 Fabric API 的事件。若 `./gradlew build` 报某事件不存在或 lambda 参数数量不符，按所用 `fabric-api` 版本调整对应一行；`FabricApiBridge.on(...)` 注册的可选事件失败只会打印警告，不影响启动。
2. **BakedModel 接口方法集**：兼容层 `BakedModelWrapper` / `ForgeBakedModels` 委托了 1.20.1 的 8 个 vanilla 方法且刻意未加 `@Override`。若 Loom 映射下接口方法名不同，增删对应委托方法即可。
3. **ShaderInstance / ResourceProvider**：`RegisterShadersEvent` 接受任意资源源；若构造 shader 时报资源缺失，确认 `assets/splineroads/shaders/core/*.json` 已随 jar 打包。
4. **ModelBakery 字段名**：`ModelHook` 通过反射查找 `Map<ResourceLocation, BakedModel>`，找不到时仅打印警告（terrain 后端降级，VBO 后端仍可用）。
5. **访问宽限**：若出现 `IllegalAccessError`（Forge 默认开放了部分 vanilla 成员），把对应成员写入 `splineroads.accesswidener` 并在 `build.gradle` 取消 `accessWidenerPath` 注释。
6. **Mixin**：`splineroads.mixins.json` 设了 `required: false` 与 `defaultRequire: 0`，注入失败不会导致崩溃；如 `setScreen` 重定向失效，地图编辑器「关闭子界面返回地图」的行为会降级。

## 已知降级 / 未完成

- **MTR 铁路路基联动**：关闭（Forge 专属依赖）。
- **Forge coremods**（`terrain_upload.js` / `mtr_snapshot.js`）：未移植。原版针对 `ChunkRenderDispatcher$RenderChunk$RebuildTask` 的 ASM 上传钩子在 Fabric 侧没有等价注入，因此 **TERRAIN 后端（依赖 shader pack 的地形材质路径）不完整**；默认 AUTO 在无 shader pack 时使用 VBO 后端。若需完整 TERRAIN 支持，需针对 Fabric 的区块重建补一个 mixin。
- **ModelData 传递**：vanilla 的 `BakedModel.getQuads(state, side, random)` 不携带 `ModelData`。兼容层在 `getModelData()` 阶段通过反射读取 `BlockEntity.getModelData()`，因此 `RoadFillEntity` 的材质/高度数据仍可进入包装模型；但 vanilla 的区块重建不会为每个位置调用 `getModelData()`，所以与 `RoadTerrainHandoff` 相关的逐 cell 路面四边形（TERRAIN 后端）仍不完整，VBO 后端不受影响。
- **爆炸保护**：`ExplosionEvent.Detonate` 未接入，道路方块可能被爆炸影响。
- **Screen / 输入 mixin**：`require = 0`，若目标方法名与本地映射不符则静默跳过。

## 许可

原版自定义非商业许可要求：保留原作者署名与许可声明、修改版明确标注为非官方、禁止商业用途。本仓库的 `LICENSE`、`THIRD_PARTY_NOTICES.md`、`THIRD_PARTY_LICENSES/`、`fabric.mod.json` 描述与 README 均已满足。
