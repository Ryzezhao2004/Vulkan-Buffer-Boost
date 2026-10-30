# VulkanBufferBoost

[中文](#中文) · [English](#english)

---

## 中文

NeoForge 26.3 客户端模组，用于消除区块渲染时的
`Cannot fit allocation of size X into staging buffer of size Y` 崩溃。

### 解决了什么问题

区块渲染的暂存缓冲区（StagingBuffer）默认上限较小，复杂网格下会触发 `Cannot fit allocation` 崩溃。
本模组在游戏类加载前用 Mixin `@ModifyConstant` 把这个上限从 98 MiB 改写成可配置的更大值，
**无需任何启动参数**，把 JAR 放进 `mods/` 即生效。

### 开发环境

| 项目 | 版本 |
| --- | --- |
| Minecraft | 26.3 |
| 模组加载器 | NeoForge 26.3.0.58-beta |
| Java | 25 |

构建：`./gradlew --offline build dist`，产物为 `ported/vulkanbufferboost-1.0.0.jar`。

### 如何配置

neoforge 模组菜单 → 选中 VulkanBufferBoost → 详情页 **Config** 按钮。

| 选项 | 说明 |
| --- | --- |
| `enabled` | 总开关；设为 `false` 关闭改写 |
| `sourceSize` | 原始常量，26.3 为 `98 MiB`，一般无需修改 |
| `targetSize` | 目标大小，默认 `128 MiB`；可写 `256MB` / `1GiB` 等直观单位，也可写裸字节 |
| `debug` | 输出详细日志 |

修改后需**完全重启游戏**生效。

---

## English

A NeoForge 26.3 client-side mod that eliminates the
`Cannot fit allocation of size X into staging buffer of size Y` crash during chunk rendering.

### What it solves

The default size limit of the chunk-rendering staging buffer (StagingBuffer) is small and triggers the
`Cannot fit allocation` crash with complex meshes. This mod rewrites that limit from 98 MiB to a
configurable larger value via Mixin `@ModifyConstant` before the game loads its classes —
**no launch arguments required**; just drop the JAR into `mods/` and it works.

### Development environment

| Item | Version |
| --- | --- |
| Minecraft | 26.3 |
| Mod loader | NeoForge 26.3.0.58-beta |
| Java | 25 |

Build: `./gradlew --offline build dist`; the output is `ported/vulkanbufferboost-1.0.0.jar`.

### How to configure

NeoForge mods menu → select VulkanBufferBoost → **Config** button on the details screen.

| Option | Description |
| --- | --- |
| `enabled` | Master switch; set to `false` to disable the rewrite |
| `sourceSize` | Original constant; 98 MiB on 26.3, usually no need to change |
| `targetSize` | Target size, default `128 MiB`; accepts intuitive units like `256MB` / `1GiB`, or raw bytes |
| `debug` | Output verbose logs |

Requires a **full game restart** to take effect after changes.
