package com.vulkanbufferboost.neoforge.mixin;

import com.vulkanbufferboost.neoforge.BoostConfig;

import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mixin(SectionRenderDispatcher.class)
public abstract class SectionRenderDispatcherMixin {

    private static final Logger LOGGER = LoggerFactory.getLogger("VulkanBufferBoost");

    @ModifyConstant(
            method = "<init>",
            constant = @Constant(intValue = 102_760_448),
            require = 1
    )
    private int patchStagingBufferSize(final int original) {
        if (!BoostConfig.isEnabled()) {
            return original;
        }
        if (!BoostConfig.isPatched()) {
            final int target = BoostConfig.getTargetSize();
            LOGGER.info("VulkanBufferBoost: 已将暂存缓冲区上限改写为 {} 字节（{} MiB）",
                    target, target >> 20);
        }
        BoostConfig.markPatched();
        return BoostConfig.getTargetSize();
    }
}
