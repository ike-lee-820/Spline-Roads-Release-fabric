# Spline Roads — 非官方 Fabric 移植版

> **Unofficial Fabric port.** 本仓库是 [ShiraAya/Spline-Roads-Release](https://github.com/ShiraAya/Spline-Roads-Release) 2.1.0（Minecraft 1.20.1 / Forge）的**非官方** Fabric 移植，与原作者无关联、未获其背书。
> 原创内容沿用 [原版自定义非商业许可](LICENSE)：允许自由使用、修改与非商业再分发，但须保留署名与许可声明、明确标注为非官方版本，**禁止商业用途**。第三方声明见 [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md)。

样条道路、路口、匝道、立交、桥隧、标志与信号灯，以及可选的道路编辑工具，全部来自原版逻辑层（385 个 Java 文件、约 5.5 万行），本移植**未改动这部分代码**。

## 环境要求

| 项目 | 版本 |
| --- | --- |
| Minecraft | 1.20.1 |
| Java | 17 |
| Fabric Loader | ≥ 0.15 |
| Fabric API | 1.20.1 对应版本（如 0.92.2+1.20.1） |

联机时客户端与服务端使用**同一版本**的模组。

## 构建

需要可访问 `maven.fabricmc.net` / Mojang 与 Maven Central 的网络、以及 JDK 17：

```bash
./gradlew build          # Windows: gradlew.bat build
```

产物：`build/libs/splineroads-fabric-2.1.0+fabric.jar`，放入 `mods/` 即可。

## 与原版的主要差异

- **加载器与映射**：ForgeGradle + Forge 事件/注册/网络 → Fabric Loom + Mojang 官方映射。官方映射保证所有 `net.minecraft.*` 名称不变，因此逻辑层零改动。
- **兼容层**：`src/main/java/net/minecraftforge/**` 是本移植自写的轻量适配实现（**不含任何 Forge 代码**），把原版用到的 Forge API 映射到 Fabric API 或等价实现。详见 [MIGRATION.md](MIGRATION.md)。
- **配置**：ForgeConfigSpec / NightConfig → `config/splineroads-fabric.json`（Gson），命令写入即时生效。
- **MTR 联动**：MTR 4.0.5 仅有 Forge 版，Fabric 端该可选功能**关闭**，相关类改为惰性存根；其余功能不受影响。
- **Forge coremods**：`coremods/*.js` 依赖 Forge 的 ASM 框架，未移植；其地形上传钩子改由 Fabric 事件桥驱动，视频模式自动回退到 VBO 后端。
- **爆炸保护**：原版 `ExplosionEvent.Detonate` 在 Fabric 无直接对应事件，此项保护当前未启用。

## 状态说明

本仓库由自动迁移流程生成，**未在可联网下载 Minecraft / Fabric 依赖的环境中执行 `./gradlew build`**。首次本地构建时请对照 [MIGRATION.md](MIGRATION.md) 的「需本地确认」清单核对 Fabric API 签名。游戏内功能请在测试世界验证后再用于存档。

## 致谢

- 原作者 **ShiraAya** —— Spline Roads 2.1.0（Forge）
- Kenney City Kit Roads 2.2（CC0）、原版 asphalt 资源
- FabricMC —— Loom、Fabric Loader、Fabric API
