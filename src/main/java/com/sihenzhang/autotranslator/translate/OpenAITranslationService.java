package com.sihenzhang.autotranslator.translate;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sihenzhang.autotranslator.AutoTranslator;
import com.sihenzhang.autotranslator.Config;
import com.sihenzhang.autotranslator.Utils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class OpenAITranslationService implements TranslationService {
    public OpenAITranslationService() {
        Preconditions.checkArgument(!Config.OPENAI_API_KEY.get().isBlank(), "OpenAI API key should not be empty");
        Preconditions.checkArgument(!Config.OPENAI_MODEL.get().isBlank(), "OpenAI model should not be empty");
    }

    @Override
    public String translate(String text, String targetLanguage) throws Exception {
        var requestBody = new JsonObject();
        requestBody.addProperty("model", Config.OPENAI_MODEL.get());

        var messages = new JsonArray();

        if (!Config.OPENAI_SYSTEM_PROMPT.get().isBlank()) {
            var systemMessage = new JsonObject();
            systemMessage.addProperty("role", "developer");
            systemMessage.addProperty("content", Config.OPENAI_SYSTEM_PROMPT.get());
            messages.add(systemMessage);
        }

        var message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", StrSubstitutor.replace(Config.getOrDefault(Config.OPENAI_SINGLE_PROMPT), Map.of("to", targetLanguage, "text", text)));
        messages.add(message);

        requestBody.add("messages", messages);

        if (!Config.OPENAI_MODEL_CONFIGURATION.get().isBlank()) {
            try {
                var modelConfiguration = JsonParser.parseString(Config.OPENAI_MODEL_CONFIGURATION.get()).getAsJsonObject();
                Utils.assign(requestBody, modelConfiguration);
            } catch (Exception e) {
                AutoTranslator.LOGGER.warn("Failed to parse OpenAI model configuration, so it will not take effect.", e);
            }
        }

        try (var httpClient = HttpClientBuilder.create().useSystemProperties().disableAutomaticRetries().build()) {
            var post = new HttpPost(Config.getOrDefault(Config.OPENAI_API_URL));
            post.setHeader("Authorization", "Bearer " + Config.OPENAI_API_KEY.get());
            post.setHeader("Content-Type", "application/json");

            var entity = new StringEntity(requestBody.toString(), ContentType.APPLICATION_JSON);
            post.setEntity(entity);

            try (var response = httpClient.execute(post)) {
                var statusCode = response.getStatusLine().getStatusCode();
                if (statusCode < 200 || statusCode >= 300) {
                    throw new IOException("Unexpected response code: " + statusCode);
                }

                var responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                var jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();

                return jsonResponse.getAsJsonArray("choices")
                        .get(0)
                        .getAsJsonObject()
                        .getAsJsonObject("message")
                        .get("content")
                        .getAsString()
                        .trim();
            }
        }
    }
}
