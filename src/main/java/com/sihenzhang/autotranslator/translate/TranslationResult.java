package com.sihenzhang.autotranslator.translate;

public record TranslationResult(
        TranslationKey translationKey,
        String translatedText
) {
    public String i18nKey() {
        return translationKey.i18nKey();
    }

    public String sourceText() {
        return translationKey.sourceText();
    }
}
