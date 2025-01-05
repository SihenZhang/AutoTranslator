package com.sihenzhang.autotranslator;

import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import org.slf4j.Logger;

@Mod(value = AutoTranslator.MOD_ID, dist = Dist.CLIENT)
public class AutoTranslator {
    public static final String MOD_ID = "autotranslator";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AutoTranslator(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::initTranslationManager);

        modContainer.registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_CONFIG);
    }

    private void initTranslationManager(final FMLClientSetupEvent event) {
        TranslationManager.init();
    }
}
