VulkanBufferBoost 1.0.0 —— 交付物
NeoForge 26.3 / Minecraft 26.3 / Java 25

【安装（零启动参数）】
1. 把 vulkanbufferboost-1.0.0.jar 直接放进 <游戏目录>/mods/
2. 启动游戏即可——无需任何 JVM 参数、无需改启动器配置

本模组通过 NeoForge 的 Mixin 在类加载阶段改写区块渲染暂存缓冲区上限，
因此在「放进 mods/ 」的那一刻就已经接线完毕，不需要 -javaagent 也不需要 -D 开关。

【配置（游戏内）】
无需编辑文件：游戏主菜单 → 模组 → 选中 VulkanBufferBoost → 详情页右下角「Config」按钮，
即可直接修改并保存。修改后必须完全重启游戏生效。

    enabled     : 总开关（false 即禁用改写）
    sourceSize  : 26.3 的原始常量，默认 98 MiB（102760448 字节），一般无需改
    targetSize  : 目标值，默认 128 MiB；可直接写 256MB / 1GiB 等单位，也可写裸字节
    debug       : 输出详细日志

配置由 NeoForge 自动持久化到 <游戏目录>/config/ 下的 *.toml（文件名由 NeoForge 管理），通常不必手写。
早期版本的 config/vulkanbufferboost.properties 已不再被读取，可删除以免混淆。

注意：走 PersistentlyMapped 路径时单次 tryAppend 上限是 targetSize/2（默认即 64 MiB），
      若仍崩溃报 "staging buffer of size 51380224"，把 targetSize 提到 256 MiB 或更大。

【确认生效】
latest.log 中搜索 [VulkanBufferBoost]，进入世界时应看到类似：
    VulkanBufferBoost: 已将暂存缓冲区上限改写为 134217728 字节（128 MiB）

【文件】
vulkanbufferboost-1.0.0.jar          单一扁平 mod JAR（Mixin + 游戏内配置）
config-sample/vulkanbufferboost.toml  配置样例（游戏内改完会自动写入 config/ 下的同结构文件）
