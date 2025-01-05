package com.sihenzhang.autotranslator.translate;

public interface TranslationService {
    /**
     * Translates the given text from sourceLanguage to targetLanguage.
     *
     * @param text           The text to be translated.
     * @param targetLanguage The target language code (e.g., "fr").
     * @return The translated text.
     * @throws Exception if translation fails or if there is any underlying service issue.
     */
    String translate(String text, String targetLanguage) throws Exception;
}
