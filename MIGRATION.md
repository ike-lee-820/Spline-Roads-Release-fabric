# Forge → Fabric 迁移说明（Spline Roads 2.1.0）

## 当前状态

✅ **编译已通过**：GitHub Actions（Ubuntu + Temurin JDK 17 + Fabric Loom 1.6）执行 `./gradlew build` 返回 `BUILD SUCCESSFUL`，产出 `splineroads-fabric-2.1.0+fabric.jar`（约 4.3 MB）与 sources jar。
⚠️ **Mixin 为运行时织入**：CI 只验证编译与注解处理，注入是否命中需游戏内实测。

## 迁移策略

1. **保持原版逻辑层基本不动**：Forge 1.20.1（ForgeGradle 6）与 Fabric Loom 均可使用 Mojang 官方映射（mojmap）。`build.gradle` 里 `mappings loom.officialMojangMappings()` 让 `net.minecraft.*` 的类名、方法名、字段名与 Forge 源码一致，385 个原版 Java 文件中绝大多数无需改动。
2. **兼容层而非全量改写**：原版只有 30 个文件直接引用 Forge API。为避免改动 5.5 万行业务代码，新增 `src/main/java/net/minecraftforge/**` 下的自写适配类（**不是 Forge 的代码**），提供同名 API，由 `com.sora.splineroads.fabric` 下的入口与事件桥驱动。
3. **Forge 对 vanilla 的 patch 逐项消解**：Forge 给若干原版类「加了」方法或构造。这些在 Fabric 上不存在，已按下表改写或由兼容层吸收。
4. **Forge coremods → Mixin**：`coremods/*.js` 依赖 Forge 的 ASM 框架，改为等价的 Fabric Mixin（见下表「coremod 对照」）。

## 目录结构

```
.github/workflows/build.yml                  CI：JDK17 + Loom，构建并回写日志/jar
build.gradle / settings.gradle / gradle.properties   Fabric Loom 工程
stubs/java/org/mtr/...                       MTR 可选 Mixin 的编译期占位（不进 jar）
src/main/resources/fabric.mod.json          入口点、依赖、非官方声明
src/main/resources/splineroads.mixins.json  Mixin 配置
src/main/java/com/sora/splineroads/**       原版代码（少量适配改动）
src/main/java/com/sora/splineroads/fabric/**  Fabric 入口、事件桥、区块跟踪、模型钩子、Mixin
src/main/java/net/minecraftforge/**          自写 Forge API 兼容层
ci-out/                                     CI 自动回写的 build.log 与 jar
```

## API 对照表

### Forge 框架 API → 兼容层 / Fabric

| Forge 侧 | Fabric 侧实现 |
| --- | --- |
| `@Mod` / `@Mod.EventBusSubscriber` / `@SubscribeEvent` | 保留注解；`ForgeEventBus` 显式扫描订阅类并分发 |
| `DeferredRegister` / `RegistryObject` | `Registry.register()` 立即注册；`RegistryObject.get()` 直接返回对象 |
| `ForgeRegistries.BLOCKS/ITEMS/BLOCK_ENTITY_TYPES` | `BuiltInRegistries.*` |
| `SimpleChannel` / `NetworkRegistry` / `PacketDistributor` | 反射调用 `ServerPlayNetworking` / `ClientPlayNetworking` 自定义通道 |
| `ForgeConfigSpec` | Gson 读写 `config/splineroads-fabric.json`；`Builder` 公开构造、`SPEC.save()` 空操作 |
| `ModList` | 真实 `FabricLoader.getModContainer(...)`，MTR 可选可见 |
| `Dist` / `DistExecutor` | `FabricLoader.getEnvironmentType()` |
| `MinecraftForge.EVENT_BUS.post` | `ForgeEventBus.post` |
| `RenderLevelStageEvent` | `WorldRenderEvents.AFTER_ENTITIES` 与 `END` |
| `TickEvent.*` | `ServerTickEvents` / `ClientTickEvents` / `WorldRenderEvents.START` |
| `RegisterCommandsEvent` / `RegisterClientCommandsEvent` | `CommandRegistrationCallback` / `ClientCommandRegistrationCallback` |
| `RegisterShadersEvent` | `CoreShaderRegistrationCallback`（`RegistrationContext.register(id, format, callback)`），三个 shader 随 vanilla shader 重载 |
| `ModelEvent.ModifyBakingResult` | `ModelHook` 反射定位 `ModelBakery` 的 baked model map 并 `replaceAll` |
| `EntityRenderersEvent` / 颜色 / 按键映射 | 对应 Fabric 注册表（反射调用） |
| `BlockEvent.BreakEvent` | `PlayerBlockBreakEvents.BEFORE` |
| `PlayerInteractEvent.RightClickBlock` | `UseBlockCallback.EVENT` |
| `ChunkWatchEvent.Watch/UnWatch` | `ChunkWatchTracker` 每 5 tick 依据玩家视距合成 |
| `ChunkEvent.Load/Unload` | `ServerChunkEvents` / `ClientChunkEvents` |
| `ExplosionEvent.Detonate` | `ExplosionMixin` 在 `Explosion.finalizeExplosion` HEAD 发布事件（列表为实时引用，`removeIf` 生效） |
| `Entity.getPersistentData()` | `PersistentData` + `PlayerDataMixin` 读写玩家 NBT（`SplineRoadsPlanner`） |
| `ModelData` / `BakedModelWrapper` / `ChunkRenderTypeSet` | 兼容层内实现；`ModelDataContext` 为 vanilla 三参数 `getQuads` 补上逐位置数据 |
| `BlockSnapshot` / `BlockEvent.EntityPlaceEvent` | 兼容层内轻量实现，用于区域保护探测 |

### Forge 注入到 vanilla 类的 API → 本次改写

| 原版写法 | 1.20.1 vanilla / 本移植 |
| --- | --- |
| `player.getPersistentData()` | `PersistentData.of(player)` |
| `state.getSoundType(level,pos,player)` | `state.getSoundType()` |
| `BlockEntity.getRenderBoundingBox()` `@Override` | 去掉 `@Override` |
| `BakedQuad.hasAmbientOcclusion()` / 6 参构造 | 删除第 6 个布尔参数，5 参构造保留 `isShade()` |
| `ModelBlockRenderer.renderModel(..., ModelData, RenderType)`（11 参） | vanilla 9 参版本 |
| `CreativeModeTab.builder()`（无参） | `CreativeModeTab.builder(Row.TOP, 0)` |
| `new ShaderInstance(provider, ResourceLocation, format)` | `CoreShaderRegistrationCallback` + 命名空间资源路径 |
| `ForgeConfigSpec.Builder()` 包私有构造 / `SPEC.save()` | 改为 public / 提供 public `save()` |

### Forge coremods → Mixin 对照

| Forge `coremods/*.js` | Fabric 替代 |
| --- | --- |
| `mtr_snapshot.js` → `RenderRails.lambda$isHoldingRailRelated$12` | `MtrRailAccess.railbedItem()` 由 `MtrReflection` 反射实现；工具判定走 `trainNode()` |
| `mtr_snapshot.js` → `BlockNode` 碰撞形状 / `dynamicBounds` | `Mtr4Adapter.nodeShape()` 反射合并 `VoxelShape` |
| `mtr_snapshot.js` → `Simulator.tick` / `Data.sync` | `MtrSimulatorMixin` / `MtrDataMixin`（`require = 0`，MTR 缺席时静默跳过） |
| `terrain_upload.js` → `RenderChunk` 加 `RoadChunkBuffers` | `RenderChunkMixin` 实现接口并暴露 `buffers` 字段 |
| `terrain_upload.js` → `ChunkRenderDispatcher` 加 `RoadChunkUploads` | `ChunkRenderDispatcherMixin` 实现接口并暴露 `toUpload` 队列 |
| `terrain_upload.js` → `RebuildTask.doTask` 的 begin/end/complete | `RebuildTaskMixin`（`require = 0`，按类型反射查字段，不依赖 mojmap 字段名） |
| `terrain_upload.js` → `lambda$doTask$0/1` 的上传与提交 | `RebuildTaskMixin` 的 `uploadChunkLayer` / `AtomicReference.set` 重定向 |
| Embeddium/Sodium 相关注入 | `RoadEmbeddiumHandoff` 已保留反射桥；Sodium 目标为可选增强，未强制注入 |

`MTR 4.0.5` 的 Fabric 版类名与 Forge 版一致，因此原版 `Mtr4Adapter` / `MtrNearby` 的算法用反射重写，不链接任何 `org.mtr.*` 编译期类型（避免硬依赖）。`stubs/java` 里的两个空类仅供 Mixin 注解处理器解析可选 target，**不会**被打进 jar。

## 构建与 CI

```bash
./gradlew build
```

`.github/workflows/build.yml` 在 push 到 `main` 或手动 `workflow_dispatch` 时运行：checkout → setup-java 17 → `chmod +x gradlew` → `./gradlew build --stacktrace --no-daemon`，并把日志与 `build/libs/*.jar` 上传为 Actions artifacts，同时回写到仓库 `ci-out/`（回写 commit 带 `[skip ci]`，不会递归触发）。

## 仍需运行时验证的部分

- **上传收据与 Embeddium**：`RebuildTaskMixin` 中与 lambda 相关的注入设为 `require = 0`（不同映射下 synthetic 方法名可能变化）。即使全部跳过，`uploadVanilla` / `commitVanilla` 会退回原版行为，VBO 后端仍可工作；但 staged buffer 与精确剔除会降级。
- **TERRAIN / VBO 双绘**：逐 cell ModelData 已恢复，若客户端配置把后端设为 TERRAIN，请确认 `RoadRenderer` 没有同时绘制 VBO 表面。
- **自定义 shader**：`CoreShaderRegistrationCallback` 在 1.20.1 的某些 Fabric API 版本会重复触发（约 60 次/重载）；注册本身幂等，但日志可能出现重复警告。
- **Screen / 输入 Mixin**：`require = 1`，若目标方法名与本地映射不符，启动会明确报错而不是静默跳过。

## 许可

原版自定义非商业许可要求：保留原作者署名与许可声明、修改版明确标注为非官方、禁止商业用途。本仓库的 `LICENSE`、`THIRD_PARTY_NOTICES.md`、`THIRD_PARTY_LICENSES/`、`fabric.mod.json` 描述与 README 均已满足。
