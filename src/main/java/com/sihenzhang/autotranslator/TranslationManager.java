package com.sihenzhang.autotranslator;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sihenzhang.autotranslator.translate.TranslationKey;
import com.sihenzhang.autotranslator.translate.TranslationResult;
import com.sihenzhang.autotranslator.translate.TranslationTask;
import com.sihenzhang.autotranslator.translate.TranslationTaskBuffer;
import com.sihenzhang.autotranslator.translate.service.OpenAITranslationService;
import com.sihenzhang.autotranslator.translate.service.TranslationService;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TranslationManager {
    private static TranslationTaskBuffer TRANSLATION_TASK_BUFFER;
    private static TranslationService TRANSLATION_SERVICE;

    private static final ExecutorService EXECUTOR = Executors.newThreadPerTaskExecutor(
            Thread.ofVirtual().name("AutoTranslator-Worker-", 0).factory()
    );

    private static final AsyncLoadingCache<TranslationKey, TranslationResult> CACHE = Caffeine.newBuilder()
            .executor(EXECUTOR)
            .buildAsync(new CacheLoader<>() {
                @Override
                public @Nullable TranslationResult load(TranslationKey key) throws Exception {
                    return TRANSLATION_SERVICE.translate(key, I18nManager.getCurrentLanguage());
                }

                @Override
                public Map<? extends TranslationKey, ? extends TranslationResult> loadAll(Set<?
                        extends TranslationKey> keys) throws Exception {
                    if (keys.isEmpty()) {
                        return Collections.emptyMap();
                    }
                    if (keys.size() == 1) {
                        var map = new LinkedHashMap<TranslationKey, TranslationResult>(2);
                        var key = keys.iterator().next();
                        var value = this.load(key);
                        if (value != null) {
                            map.put(key, value);
                        }
                        return Collections.unmodifiableMap(map);
                    }
                    return TRANSLATION_SERVICE.translateBatch(Set.copyOf(keys), I18nManager.getCurrentLanguage());
                }
            });

    public static void init() {
        try {
            TRANSLATION_SERVICE = switch (Config.SERVICE.get()) {
                case OPENAI -> new OpenAITranslationService();
            };
            TRANSLATION_TASK_BUFFER = new TranslationTaskBuffer(
                    Config.MAX_BATCH_SIZE.get(),
                    Config.SCHEDULER_INTERVAL.get()
            );
        } catch (Exception e) {
            AutoTranslator.LOGGER.error("Failed to initialize translation service", e);
        }
    }

    public static void translate(TranslationTask translationTask) {
        if (I18nManager.getCurrentLanguage() == null) {
            return;
        }
        CACHE.get(translationTask.translationKey()).thenAccept((result) -> {
            if (translationTask.callback() != null) {
                translationTask.callback().accept(result);
            }
        }).exceptionally(e -> {
            AutoTranslator.LOGGER.error("Failed to translate for {}", translationTask.translationKey(), e);
            return null;
        });
    }

    public static void translateBatch(List<TranslationTask> translationTasks) {
        if (I18nManager.getCurrentLanguage() == null) {
            return;
        }
        var translationKeys = translationTasks.stream().map(TranslationTask::translationKey).toList();
        CACHE.getAll(translationKeys).thenAccept((results) -> results.forEach((key, value) -> translationTasks.stream()
                .filter(task -> task.translationKey().equals(key))
                .forEach(task -> {
                    if (task.callback() != null) {
                        task.callback().accept(value);
                    }
                })
        )).exceptionally(e -> {
            AutoTranslator.LOGGER.error("Failed to translate for {}", translationKeys, e);
            return null;
        });
    }

    public static boolean isReady() {
        return TRANSLATION_TASK_BUFFER != null && TRANSLATION_SERVICE != null;
    }

    public static TranslationResult getIfPresent(TranslationKey translationKey) {
        return CACHE.synchronous().getIfPresent(translationKey);
    }

    public static void addTranslationTask(TranslationTask translationTask) {
        if (TRANSLATION_TASK_BUFFER != null) {
            TRANSLATION_TASK_BUFFER.add(translationTask);
        } else {
            translate(translationTask);
        }
    }
}
