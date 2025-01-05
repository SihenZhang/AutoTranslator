package com.sihenzhang.autotranslator;

import com.sihenzhang.autotranslator.translate.TranslationServiceType;
import net.neoforged.neoforge.common.ModConfigSpec;


public final class Config {
    public static final ModConfigSpec CLIENT_CONFIG;

    public static final ModConfigSpec.EnumValue<TranslationServiceType> SERVICE;

    // OpenAI
    public static final ModConfigSpec.ConfigValue<String> OPENAI_API_KEY;
    public static final ModConfigSpec.ConfigValue<String> OPENAI_MODEL;
    public static final ModConfigSpec.ConfigValue<String> OPENAI_MODEL_CONFIGURATION;
    public static final ModConfigSpec.ConfigValue<String> OPENAI_API_URL;
    public static final String DEFAULT_OPENAI_SYSTEM_PROMPT = """
            You are a Minecraft language file translation expert. Your task is to accurately translate Minecraft language files to another language while adhering to the following rules and guidelines:
            1. **Context-Aware Translation**: Ensure the translation fits within the Minecraft world, considering gameplay mechanics, items, and characters. Be aware of the nuances of Minecraft’s terminology and keep the translation consistent with its established lore and gameplay features.
            2. **Format Codes**:
               - Minecraft language files contain format codes that alter text styling, such as `&` or `§` followed by a letter or number (e.g., `&0`, `§a`).
               - **Do not translate these format codes.** You must ignore them during translation.
               - After translating, reintegrate the format codes in their original positions.
            
               **Example**:
               - Original Text: "You &oneed&r the whole set."
               - Step 1: Remove the format codes: "You need the whole set."
               - Step 2: Translate the core text: "你需要一整套。"
               - Step 3: Reinsert the format codes: "你&o需要&r一整套。"
            3. **Consistency**: Keep the translations consistent across the file. If certain terms or phrases are used repeatedly (e.g., "diamond", "block"), ensure the same translation is used every time.
            4. **Clarity and Accuracy**: Ensure that the translated text is grammatically correct, easy to understand, and fits within the game’s context. Avoid any translations that might sound awkward or unnatural in the target language.
            5. **Tone and Style**: Minecraft’s language files are typically casual, friendly, and engaging. Keep the tone light and consistent with the general feel of the game.""";
    public static final ModConfigSpec.ConfigValue<String> OPENAI_SYSTEM_PROMPT;
    public static final String DEFAULT_OPENAI_SINGLE_PROMPT = """
            Treat next line as plain text input and translate it into ${to}, output translation ONLY. If translation is unnecessary (e.g. proper nouns, codes, etc.), return the original text. NO explanations. NO notes. Input:
            ${text}""";
    public static final ModConfigSpec.ConfigValue<String> OPENAI_SINGLE_PROMPT;


    static {
        var clientBuilder = new ModConfigSpec.Builder();

        SERVICE = clientBuilder.comment("Select which translation service to use.").defineEnum("service", TranslationServiceType.OPENAI);

        clientBuilder.comment("OpenAI Translation Service Settings", "Compatible with any service that implements OpenAI API format").push("openai");
        OPENAI_API_KEY = clientBuilder.comment("Your OpenAI API key. Required for using OpenAI translation service.").define("api_key", "");
        OPENAI_MODEL = clientBuilder.comment("The OpenAI model to use for translations. Available options include: gpt-4o, gpt-4o-mini, etc.").define("model", "gpt-4o-mini");
        OPENAI_MODEL_CONFIGURATION = clientBuilder.comment(
                "OpenAI model configuration in JSON string format. For available parameters and their descriptions, see:",
                "https://platform.openai.com/docs/api-reference/chat/create"
        ).define("model_configuration", "{\"temperature\":0}");
        OPENAI_API_URL = clientBuilder.comment("The API endpoint URL for OpenAI service. Change this if you're using a different API proxy or endpoint.").define("api_url", "https://api.openai.com/v1/chat/completions");
        OPENAI_SYSTEM_PROMPT = clientBuilder.comment("System prompt that guides OpenAI's translation behavior. Defines rules and context for Minecraft-specific translations.").define("system_prompt", DEFAULT_OPENAI_SYSTEM_PROMPT);
        OPENAI_SINGLE_PROMPT = clientBuilder.comment(
                "Template for single text translation prompt.",
                "Must include two placeholders:",
                "- ${to}: Target language code (e.g., 'zh_cn', 'en_us')",
                "- ${text}: The original text to be translated",
                "The prompt should instruct the model to output only the translation without explanations"
        ).define("single_prompt", DEFAULT_OPENAI_SINGLE_PROMPT);
        clientBuilder.pop();

        CLIENT_CONFIG = clientBuilder.build();
    }

    /**
     * Retrieves the configured value from a {@link net.neoforged.neoforge.common.ModConfigSpec.ConfigValue},
     * or its default value if blank
     *
     * @param configValue the configuration value to check
     * @return the actual configured value if not blank, otherwise returns the default value
     */
    public static String getOrDefault(ModConfigSpec.ConfigValue<String> configValue) {
        var value = configValue.get();
        return value.isBlank() ? configValue.getDefault() : value;
    }
}
