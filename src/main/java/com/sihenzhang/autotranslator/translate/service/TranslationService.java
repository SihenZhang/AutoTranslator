package com.sihenzhang.autotranslator.translate.service;

import com.sihenzhang.autotranslator.translate.TranslationKey;
import com.sihenzhang.autotranslator.translate.TranslationResult;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;

public interface TranslationService {
    default TranslationResult translate(@Nullable String translationKey, String originalText, String targetLanguage) throws Exception {
        return translate(new TranslationKey(translationKey, originalText), targetLanguage);
    }

    TranslationResult translate(TranslationKey translationKey, String targetLanguage) throws Exception;

    Map<TranslationKey, TranslationResult> translateBatch(Set<TranslationKey> translationKeys, String targetLanguage) throws Exception;
}
