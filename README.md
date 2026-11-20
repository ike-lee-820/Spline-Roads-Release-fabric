# Spline Roads — 非官方 Fabric 移植版

> **Unofficial Fabric port.** 本仓库是 [ShiraAya/Spline-Roads-Release](https://github.com/ShiraAya/Spline-Roads-Release) 2.1.0（Minecraft 1.20.1 / Forge）的**非官方** Fabric 移植，与原作者无关联、未获其背书。
> 原创内容沿用 [原版自定义非商业许可](LICENSE)：允许自由使用、修改与非商业再分发，但须保留署名与许可声明、明确标注为非官方版本，**禁止商业用途**。第三方声明见 [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md)。

样条道路、路口、匝道、立交、桥隧、标志与信号灯，以及可选的道路编辑工具，全部来自原版逻辑层（385 个 Java 文件、约 5.5 万行）。

## 环境要求

| 项目 | 版本 |
| --- | --- |
| Minecraft | 1.20.1 |
| Java | 17 |
| Fabric Loader | ≥ 0.15 |
| Fabric API | 1.20.1 对应版本（如 0.92.2+1.20.1） |
| MTR（可选） | `FABRIC-4.0.5+1.20.1` |

联机时客户端与服务端使用**同一版本**的模组。铁路路基联动为可选功能，未装 MTR 时相关工具会给出明确提示，其余功能不受影响。

## 直接安装（已构建好的 jar）

GitHub Actions（JDK 17 + Fabric Loom）构建：

- **Actions 页面** → 最新一次 `Build Fabric Mod` → `splineroads-fabric-jars` 产物
- 或仓库的 [`ci-out/`](ci-out/) 目录（CI 自动回写）
- 或 [Releases](https://github.com/ike-lee-820/Spline-Roads-Release-fabric/releases)

把 `splineroads-fabric-2.1.0+fabric.jar` 放进 `mods/` 即可（`-sources.jar` 是源码包，不必安装）。

## 本地构建

```bash
./gradlew build          # Windows: gradlew.bat build
```

产物：`build/libs/splineroads-fabric-2.1.0+fabric.jar`。
推送到 `main` 会自动触发 [.github/workflows/build.yml](.github/workflows/build.yml)。

## 与原版的主要差异

- **加载器与映射**：ForgeGradle + Forge 事件/注册/网络 → Fabric Loom + Mojang 官方映射。官方映射保证所有 `net.minecraft.*` 名称不变，因此逻辑层基本零改动。
- **兼容层**：`src/main/java/net/minecraftforge/**` 是本移植自写的轻量适配实现（**不含任何 Forge 代码**），把原版用到的 Forge API 映射到 Fabric API 或等价实现。详见 [MIGRATION.md](MIGRATION.md)。
- **配置**：ForgeConfigSpec / NightConfig → `config/splineroads-fabric.json`（Gson），命令写入即时生效。
- **Forge 对原版的补丁**：`getPersistentData()`、`BakedQuad` 6 参构造、`CreativeModeTab.builder()` 无参版等逐项改写或由兼容层吸收，见 MIGRATION 对照表。

## 功能恢复情况（原「已知降级」）

早期移植版曾关闭/降级的功能，现已逐项加回：

- ✅ **MTR 铁路路基联动**：MTR 4.0.5 其实有 Fabric 版（`FABRIC-4.0.5+1.20.1`）。改为反射式适配 + 可选 Mixin 注入 `Simulator.tick` / `Data.sync`，无需把 MTR jar 编进工程；`fabric.mod.json` 已声明为 `suggests`。
- ✅ **爆炸保护**：Mixin 注入 `Explosion.finalizeExplosion`，在破坏前把道路碰撞块与相连方块从爆炸列表移除。
- ✅ **规划器设置持久化**：`PlayerDataMixin` 把设置写进玩家 NBT，重启后保留。
- ✅ **自定义 shader**：改用 Fabric `CoreShaderRegistrationCallback`，三个 `road_world_*` 核心 shader 随资源重载正确注册，不再依赖启动时猜测。
- ✅ **Screen / 输入 Mixin**：`require = 1`，注入失败会明确报错而不是静默降级。
- ✅ **逐 cell ModelData / TERRAIN 后端**：`BlockRenderDispatcherMixin` 在区块重建时计算 `BakedModel.getModelData(level,pos,state)`，`ModelDataContext` 把它传给 vanilla 的三参数 `getQuads`，路面四边形重新进入区块网格；并用 Mixin 替代 Forge `terrain_upload.js` 的上传收据与 `RenderChunk` / `ChunkRenderDispatcher` 接口注入。

## 状态说明

编译由 CI 验证（`BUILD SUCCESSFUL`）。**Mixin 属于运行时织入，编译通过不等于游戏内实测**；首次使用请在测试世界验证后再用于正式存档。仍需运行时确认的部分见 [MIGRATION.md](MIGRATION.md)。

## 致谢

- 原作者 **ShiraAya** —— Spline Roads 2.1.0（Forge）
- Kenney City Kit Roads 2.2（CC0）、原版 asphalt 资源
- FabricMC —— Loom、Fabric Loader、Fabric API
