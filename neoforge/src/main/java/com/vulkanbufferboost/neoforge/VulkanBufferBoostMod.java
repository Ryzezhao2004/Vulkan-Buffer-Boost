package com.vulkanbufferboost.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(value = VulkanBufferBoostMod.MOD_ID, dist = Dist.CLIENT)
public final class VulkanBufferBoostMod {

    public static final String MOD_ID = "vulkanbufferboost";

    private static final Logger LOGGER = LoggerFactory.getLogger("VulkanBufferBoost");

    public VulkanBufferBoostMod(final ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.LOCAL, BoostConfig.SPEC);

        modContainer.registerExtensionPoint(IConfigScreenFactory.class,
                (mod, parent) -> new ConfigurationScreen(mod, parent));

        LOGGER.info("VulkanBufferBoost 已加载：配置可在游戏内模组设置（Config）中调整，"
                + "将在第一次进入世界时改写 SectionRenderDispatcher 的暂存缓冲区上限。");
    }
}
