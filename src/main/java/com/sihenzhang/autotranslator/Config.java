package com.sihenzhang.autotranslator;

import com.google.common.collect.Lists;
import com.sihenzhang.autotranslator.translate.service.TranslationServiceType;
import net.neoforged.neoforge.common.ModConfigSpec;


public final class Config {
    public static final ModConfigSpec CLIENT_CONFIG;

    public static final ModConfigSpec.EnumValue<TranslationServiceType> SERVICE;
    public static final ModConfigSpec.IntValue MAX_BATCH_SIZE;
    public static final ModConfigSpec.IntValue SCHEDULER_INTERVAL;

    // OpenAI
    public static final ModConfigSpec.ConfigValue<String> OPENAI_API_KEY;
    public static final ModConfigSpec.ConfigValue<String> OPENAI_MODEL;
    public static final ModConfigSpec.ConfigValue<String> OPENAI_MODEL_CONFIGURATION;
    public static final ModConfigSpec.ConfigValue<String> OPENAI_API_URL;
    public static final ModConfigSpec.ConfigValue<String> OPENAI_SYSTEM_MESSAGE_ROLE;
    public static final String DEFAULT_OPENAI_SYSTEM_PROMPT = """
            You are a machine translation engine for Minecraft in-game texts. Your task is to accurately translate Minecraft in-game texts to another language while adhering to the following rules and guidelines:
            
            1. **Context-Aware Translation**: Ensure that the translation fits within the Minecraft world, considering gameplay mechanics, items, characters, or other in-game events. Be mindful of Minecraft’s terminology, ensuring consistency with its established lore and gameplay features.
            
            2. **Formatting Codes**:
               - Minecraft in-game texts include formatting codes that alter text styling, such as `§` or `&` followed by a letter or number (e.g., `§0`, `&a`).
               - **Do NOT translate these formatting codes.** You must ignore them during translation.
               - After translating, reintegrate the formatting codes into their original positions.
            
               **Example**:
               - Original Text: "Right-click to §bopen§r the §6chest§r."
               - Step 1: Remove the formatting codes: "Right-click to open the chest."
               - Step 2: Translate the core text: "右键点击打开箱子。"
               - Step 3: Reinsert the formatting codes: "右键点击§b打开§r§6箱子§r。"
            
            3. **Consistency**: Maintain consistency across all translated text. If certain terms or phrases are used repeatedly (e.g., "diamond", "block"), ensure that the same translated text is used each time.
            
            4. **Clarity and Accuracy**: Ensure the translated text is grammatically correct, easy to understand, and appropriate for the game’s context. Avoid translations that sound awkward or unnatural in the target language.
            
            5. **Tone and Style**: Minecraft’s in-game texts are typically casual, friendly, and engaging. Maintain a light and approachable tone that is consistent with the overall feel of the game.""";
    public static final ModConfigSpec.ConfigValue<String> OPENAI_SYSTEM_PROMPT;
    public static final String DEFAULT_OPENAI_SINGLE_PROMPT = """
            ;; Treat next line as plain text input and translate it into ${to}}, output translation ONLY. If translation is unnecessary (e.g. proper nouns, codes, etc.), return the original text. NO explanations. NO notes. Input:
            ${text}""";
    public static final ModConfigSpec.ConfigValue<String> OPENAI_SINGLE_PROMPT;
    public static final String DEFAULT_OPENAI_MULTIPLE_PROMPT = """
            You will be given a YAML formatted input containing entries with "id" and "text" fields. Here is the input:
            
            <yaml>
            ${yaml}
            </yaml>
            
            For each entry in the YAML, translate the contents of the "text" field into ${to}. Write the translation back into the "text" field for that entry.
            
            Here is an example of the expected format:
            
            <example>
            Input:
              - id: 1
                text: Source
            Output:
              - id: 1
                text: Translation
            </example>
            
            Please return the translated YAML directly without wrapping <yaml> tag or include any additional information.""";
    public static final ModConfigSpec.ConfigValue<String> OPENAI_MULTIPLE_PROMPT;


    static {
        var clientBuilder = new ModConfigSpec.Builder();

        SERVICE = clientBuilder.comment("Select which translation service to use.").defineEnum("service", TranslationServiceType.OPENAI);
        MAX_BATCH_SIZE = clientBuilder.comment(
                "Maximum number of texts that can be translated in a single batch request.",
                "Higher value will improve efficiency but may exceed API limits and increase response time.",
                "Set to 1 to disable batch translation so that each text will be translated individually."
        ).defineInRange("max_batch_size", 10, 1, Integer.MAX_VALUE);
        SCHEDULER_INTERVAL = clientBuilder.comment(
                "Time interval (in milliseconds) for the batch translation scheduler.",
                "The scheduler ensures partially filled batches are translated after this interval, even if the number of pending texts hasn't reached max_batch_size.",
                "Lower value will cause frequent small-batch requests, effectively degrading batch translation into individual translations.",
                "Higher value will increase waiting time for incomplete batches, resulting in delayed translations for users.",
                "Only takes effect when batch translation is enabled (max_batch_size > 1)."
        ).defineInRange("scheduler_interval", 1000, 1, Integer.MAX_VALUE);

        clientBuilder.comment("OpenAI Translation Service Settings", "Compatible with any service that implements OpenAI API format").push("openai");
        OPENAI_API_KEY = clientBuilder.comment("Your OpenAI API key. Required for using OpenAI translation service.").define("api_key", "");
        OPENAI_MODEL = clientBuilder.comment(
                "The OpenAI model to use for translations. For available models and their descriptions, see:",
                "https://platform.openai.com/docs/models"
        ).define("model", "gpt-4o-mini");
        OPENAI_MODEL_CONFIGURATION = clientBuilder.comment(
                "OpenAI model configuration in JSON string format. For available parameters and their descriptions, see:",
                "https://platform.openai.com/docs/api-reference/chat/create"
        ).define("model_configuration", "{\"temperature\":0}");
        OPENAI_API_URL = clientBuilder.comment("The API endpoint URL for OpenAI service. Change this if you're using a different API proxy or endpoint.").define("api_url", "https://api.openai.com/v1/chat/completions");
        OPENAI_SYSTEM_MESSAGE_ROLE = clientBuilder.comment("System Message Role").defineInList("system_message_role", "developer", Lists.newArrayList("developer", "system"));
        OPENAI_SYSTEM_PROMPT = clientBuilder.comment("System prompt that guides OpenAI's translation behavior. Defines rules and context for Minecraft-specific translations.").define("system_prompt", DEFAULT_OPENAI_SYSTEM_PROMPT);
        OPENAI_SINGLE_PROMPT = clientBuilder.comment(
                "Template for single text translation prompt.",
                "Must include two placeholders:",
                "- ${to}: Target language code (e.g., 'zh_cn', 'en_us')",
                "- ${text}: The original text to be translated",
                "The prompt should instruct the model to output only the translated text directly without any additional information."
        ).define("single_prompt", DEFAULT_OPENAI_SINGLE_PROMPT);
        OPENAI_MULTIPLE_PROMPT = clientBuilder.comment(
                "Template for batch text translation prompt.",
                "Must include two placeholders:",
                "- ${to}: Target language code (e.g., 'zh_cn', 'en_us')",
                "- ${yaml}: The YAML input to be translated",
                "The prompt should instruct the model to output only the translated YAML directly without any additional information."
        ).define("multiple_prompt", DEFAULT_OPENAI_MULTIPLE_PROMPT);
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
