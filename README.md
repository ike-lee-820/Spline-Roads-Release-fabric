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

联机时客户端与服务端使用**同一版本**的模组。

## 直接安装（已构建好的 jar）

已通过 GitHub Actions（JDK 17 + Fabric Loom）成功构建：

- **Actions 页面** → 最新一次 `Build Fabric Mod` → `splineroads-fabric-jars` 产物
- 或仓库的 [`ci-out/`](ci-out/) 目录（CI 自动回写）

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
- **MTR 联动**：MTR 4.0.5 仅有 Forge 版，Fabric 端该可选功能**关闭**，相关类改为惰性存根；其余功能不受影响。
- **Forge coremods**：`coremods/*.js` 依赖 Forge 的 ASM 框架，未移植；其地形上传钩子改由 Fabric 事件桥驱动，视频模式自动回退到 VBO 后端。
- **爆炸保护**：原版 `ExplosionEvent.Detonate` 在 Fabric 无直接对应事件，此项保护当前未启用。
- **规划器设置**：原版 `Entity.getPersistentData()` 是 Forge 注入的方法，这里改为进程内存储，**服务端重启后不保留**。

## 状态说明

编译已由 CI 验证通过（`BUILD SUCCESSFUL`），但**尚未在游戏内实测**。首次使用请在测试世界验证后再用于正式存档；已知降级见 [MIGRATION.md](MIGRATION.md)。

## 致谢

- 原作者 **ShiraAya** —— Spline Roads 2.1.0（Forge）
- Kenney City Kit Roads 2.2（CC0）、原版 asphalt 资源
- FabricMC —— Loom、Fabric Loader、Fabric API
