package com.sihenzhang.autotranslator;

import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.locale.Language;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.List;

public class I18nManager {
    private static Language CURRENT_LANGUAGE;
    private static String CURRENT_LANGUAGE_CODE;

    public static void loadFrom(ResourceManager resourceManager, List<String> filenames, boolean defaultRightToLeft) {
        if (!filenames.isEmpty()) {
            var currentLanguageCode = filenames.getLast();
            CURRENT_LANGUAGE_CODE = currentLanguageCode;
            CURRENT_LANGUAGE = ClientLanguage.loadFrom(resourceManager, List.of(currentLanguageCode), defaultRightToLeft);
        } else {
            CURRENT_LANGUAGE = null;
            CURRENT_LANGUAGE_CODE = null;
        }
    }

    public static boolean hasInCurrentLanguage(String id) {
        return CURRENT_LANGUAGE != null && CURRENT_LANGUAGE.has(id);
    }

    public static String getCurrentLanguage() {
        return CURRENT_LANGUAGE_CODE;
    }
}
