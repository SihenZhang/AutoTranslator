package com.sihenzhang.autotranslator.translate;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public record TranslationTask(
        TranslationKey translationKey,
        @Nullable Consumer<TranslationResult> callback
) {
    public TranslationTask(String i18nKey, String sourceText) {
        this(new TranslationKey(i18nKey, sourceText), null);
    }
}
