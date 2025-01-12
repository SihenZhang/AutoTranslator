package com.sihenzhang.autotranslator.translate.service;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sihenzhang.autotranslator.AutoTranslator;
import com.sihenzhang.autotranslator.Config;
import com.sihenzhang.autotranslator.Utils;
import com.sihenzhang.autotranslator.translate.TranslationKey;
import com.sihenzhang.autotranslator.translate.TranslationResult;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class OpenAITranslationService implements TranslationService {
    public OpenAITranslationService() {
        Preconditions.checkArgument(!Config.OPENAI_API_KEY.get().isBlank(), "OpenAI API key should not be empty");
        Preconditions.checkArgument(!Config.OPENAI_MODEL.get().isBlank(), "OpenAI model should not be empty");
    }

    @Override
    public TranslationResult translate(TranslationKey translationKey, String targetLanguage) throws Exception {
        var result = this.sendOpenAIRequest(
                StrSubstitutor.replace(
                        Config.getOrDefault(Config.OPENAI_SINGLE_PROMPT),
                        Map.of("to", targetLanguage, "text", translationKey.sourceText())
                )
        );
        return new TranslationResult(translationKey, result);
    }

    @Override
    public Map<TranslationKey, TranslationResult> translateBatch(Set<TranslationKey> translationRequests, String targetLanguage) throws Exception {
        Map<Integer, TranslationKey> requestIdMap = new HashMap<>();
        List<Map<String, Object>> translationItems = new ArrayList<>();

        var requestId = 1;
        for (var translationRequest : translationRequests) {
            requestIdMap.put(requestId, translationRequest);
            translationItems.add(Map.of("id", requestId, "text", translationRequest.sourceText()));
            requestId++;
        }

        var yamlRequest = Utils.toYaml(translationItems);
        var translatedResponse = this.sendOpenAIRequest(
                StrSubstitutor.replace(
                        Config.getOrDefault(Config.OPENAI_MULTIPLE_PROMPT),
                        Map.of("to", targetLanguage, "yaml", yamlRequest)
                )
        );

        List<Map<String, Object>> translatedItems = Utils.parseYaml(translatedResponse);
        Map<TranslationKey, TranslationResult> translationResults = new HashMap<>();

        translatedItems.forEach(item -> {
            var id = (Integer) item.get("id");
            var translatedText = (String) item.get("text");
            var originalKey = requestIdMap.get(id);
            translationResults.put(originalKey, new TranslationResult(originalKey, translatedText));
        });

        return translationResults;
    }


    private String sendOpenAIRequest(String userMessage) throws Exception {
        var requestBody = new JsonObject();
        requestBody.addProperty("model", Config.OPENAI_MODEL.get());

        var messages = new JsonArray();

        if (!Config.OPENAI_SYSTEM_PROMPT.get().isBlank()) {
            var systemMessage = new JsonObject();
            systemMessage.addProperty("role", Config.OPENAI_SYSTEM_MESSAGE_ROLE.get());
            systemMessage.addProperty("content", Config.OPENAI_SYSTEM_PROMPT.get());
            messages.add(systemMessage);
        }

        var message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", userMessage);
        messages.add(message);

        requestBody.add("messages", messages);

        if (!Config.OPENAI_MODEL_CONFIGURATION.get().isBlank()) {
            try {
                var modelConfiguration =
                        JsonParser.parseString(Config.OPENAI_MODEL_CONFIGURATION.get()).getAsJsonObject();
                Utils.assign(requestBody, modelConfiguration);
            } catch (Exception e) {
                AutoTranslator.LOGGER.warn("Failed to parse OpenAI model configuration, so it will not take effect.",
                        e);
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

                AutoTranslator.LOGGER.info("OpenAI request: {}\nOpenAI response: {}", requestBody, jsonResponse);

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
