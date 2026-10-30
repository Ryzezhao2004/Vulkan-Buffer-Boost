package com.vulkanbufferboost.neoforge;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.neoforged.neoforge.common.ModConfigSpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BoostConfig {

    public static final int DEFAULT_SOURCE_SIZE = 102_760_448;
    public static final int DEFAULT_TARGET_SIZE = 134_217_728;
    public static final int MIN_SIZE = 1 << 20;
    public static final int MAX_SIZE = 1 << 30;

    private static final Pattern SIZE_PATTERN = Pattern.compile(
            "^(\\d+)\\s*(KiB|MiB|GiB|KB|MB|GB|K|M|G|B)?$",
            Pattern.CASE_INSENSITIVE);

    public static final ModConfigSpec SPEC;
    public static final Config CONFIG;

    static {
        final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        CONFIG = new Config(builder);
        SPEC = builder.build();
    }

    private static final Logger LOG = LoggerFactory.getLogger("VulkanBufferBoost");

    private static volatile boolean patched;

    public static final class Config {
        public final ModConfigSpec.BooleanValue enabled;
        public final ModConfigSpec.ConfigValue<String> sourceSize;
        public final ModConfigSpec.ConfigValue<String> targetSize;
        public final ModConfigSpec.BooleanValue debug;

        Config(final ModConfigSpec.Builder builder) {
            builder.comment(
                    "VulkanBufferBoost 配置",
                    "修改后需要完全重启游戏才会生效。");

            enabled = builder
                    .comment(
                            "是否启用字节码改写。关闭后不会修改区块渲染暂存缓冲区大小。",
                            "本配置在进入世界、首次改写缓冲区时读取，修改后需要完全重启游戏才会生效。")
                    .translation("vulkanbufferboost.configuration.enabled")
                    .define("enabled", true);

            sourceSize = builder
                    .comment(
                            "需要被替换掉的原始常量（26.3 为 98 MiB）。一般无需修改。",
                            "支持单位写法：如 98 MiB / 100MB，或裸字节（如 102760448）。",
                            "修改后需要完全重启游戏才会生效。")
                    .translation("vulkanbufferboost.configuration.sourceSize")
                    .define("sourceSize", "98 MiB");

            targetSize = builder
                    .comment(
                            "替换后的目标缓冲区大小。可直接写直观单位（如 128 MiB / 256MB / 1GiB），也可写裸字节。",
                            "CPU 暂存模式下单次 tryAppend 上限 = targetSize；",
                            "PersistentlyMapped 模式下上限 = targetSize / 2（MappableRingBuffer 以 bufferSize/2 建多个缓冲）。",
                            "默认 128 MiB。若仍崩溃，请调大（如 256 MiB / 512 MiB）。",
                            "修改后需要完全重启游戏才会生效。")
                    .translation("vulkanbufferboost.configuration.targetSize")
                    .define("targetSize", "128 MiB");

            debug = builder
                    .comment("输出详细日志。")
                    .translation("vulkanbufferboost.configuration.debug")
                    .define("debug", false);
        }
    }

    public static boolean isEnabled() {
        return CONFIG.enabled.get();
    }

    public static boolean isDebug() {
        return CONFIG.debug.get();
    }

    public static int getSourceSize() {
        return clamp(parseSize(CONFIG.sourceSize.get(), DEFAULT_SOURCE_SIZE, "sourceSize"));
    }

    public static int getTargetSize() {
        final int source = getSourceSize();
        int target = clamp(parseSize(CONFIG.targetSize.get(), DEFAULT_TARGET_SIZE, "targetSize"));
        if (target < source) {
            LOG.warn("targetSize({}) 小于 sourceSize({})，已回退为 sourceSize（缓冲区不会变小）", target, source);
            target = source;
        }
        return target;
    }

    public static void markPatched() {
        patched = true;
    }

    public static boolean isPatched() {
        return patched;
    }

    private static int parseSize(final String raw, final int fallback, final String key) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        final String trimmed = raw.trim().replace("_", "");
        final Matcher matcher = SIZE_PATTERN.matcher(trimmed);
        if (!matcher.matches()) {
            LOG.warn("无法解析 {} = '{}'，使用默认值 {}", key, raw, fallback);
            return fallback;
        }
        long value;
        try {
            value = Long.parseLong(matcher.group(1));
        } catch (final NumberFormatException e) {
            LOG.warn("无法解析 {} = '{}'，使用默认值 {}", key, raw, fallback);
            return fallback;
        }
        final String unit = matcher.group(2);
        if (unit != null) {
            switch (unit.toUpperCase(Locale.ROOT)) {
                case "K", "KI", "KIB", "KB" -> value *= 1024L;
                case "M", "MI", "MIB", "MB" -> value *= 1024L * 1024L;
                case "G", "GI", "GIB", "GB" -> value *= 1024L * 1024L * 1024L;
                default -> { }
            }
        }
        if (value > Integer.MAX_VALUE) {
            LOG.warn("{} = '{}' 超出 int 范围，使用默认值 {}", key, raw, fallback);
            return fallback;
        }
        return (int) value;
    }

    private static int clamp(final int value) {
        if (value < MIN_SIZE) {
            LOG.warn("缓冲区大小 {} 小于下限 {}，已提升", value, MIN_SIZE);
            return MIN_SIZE;
        }
        if (value > MAX_SIZE) {
            LOG.warn("缓冲区大小 {} 超过上限 {}，已压低", value, MAX_SIZE);
            return MAX_SIZE;
        }
        return value;
    }
}
