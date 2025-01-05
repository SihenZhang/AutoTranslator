package com.sihenzhang.autotranslator;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.sihenzhang.autotranslator.translate.OpenAITranslationService;
import com.sihenzhang.autotranslator.translate.TranslationService;
import net.minecraft.Util;

import java.util.concurrent.CompletableFuture;

public class TranslationManager {
    private static TranslationService TRANSLATION_SERVICE = null;

    public static final Cache<String, String> CACHE = CacheBuilder.newBuilder().build();

    public static void init() {
        try {
            TRANSLATION_SERVICE = switch (Config.SERVICE.get()) {
                case OPENAI -> new OpenAITranslationService();
            };
        } catch (Exception e) {
            AutoTranslator.LOGGER.error("Failed to initialize translation service", e);
        }
    }

    public static CompletableFuture<String> translate(String key, String sourceText) {
        if (TRANSLATION_SERVICE == null) {
            return CompletableFuture.completedFuture(null);
        }
        var currentLang = I18nManager.getCurrentLanguage();
        if (currentLang == null) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.supplyAsync(() -> {
            try {
                return CACHE.get(key, () -> TRANSLATION_SERVICE.translate(sourceText, currentLang));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, Util.nonCriticalIoPool()).exceptionally(e -> {
            AutoTranslator.LOGGER.error("Failed to translate {} to {}", sourceText, currentLang, e);
            return null;
        });
    }
}

