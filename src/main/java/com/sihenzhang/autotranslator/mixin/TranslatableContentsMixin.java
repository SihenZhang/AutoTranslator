package com.sihenzhang.autotranslator.mixin;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.sihenzhang.autotranslator.I18nManager;
import com.sihenzhang.autotranslator.TranslationManager;
import com.sihenzhang.autotranslator.Utils;
import com.sihenzhang.autotranslator.WorldLoadStateManager;
import com.sihenzhang.autotranslator.translate.TranslationKey;
import com.sihenzhang.autotranslator.translate.TranslationTask;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.chat.contents.TranslatableFormatException;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

@Mixin(TranslatableContents.class)
public abstract class TranslatableContentsMixin {
    @Shadow
    @Final
    private String key;
    @Shadow
    private List<FormattedText> decomposedParts;
    @Shadow
    @Final
    @Nullable
    private String fallback;

    @Shadow
    protected abstract void decomposeTemplate(String formatTemplate, Consumer<FormattedText> consumer);

    @ModifyExpressionValue(
            method = "decompose()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/locale/Language;getOrDefault(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;",
                    ordinal = 0
            )
    )
    private String modifyWithFallback(String original) {
        if (!TranslationManager.isReady() || WorldLoadStateManager.isWorldLoading()) {
            return original;
        }
        // No translation needed in the following cases:
        // 1. The key already has localized content in current language, indicating the text is already localized
        // 2. The text is a fallback value
        // 3. The text is identical to the key, indicating missing localization in default language
        var needTranslate = !I18nManager.hasInCurrentLanguage(this.key) &&
                !(this.fallback != null && this.fallback.equals(original)) &&
                !this.key.equals(original);
        if (needTranslate && Utils.hasTranslatableText(original)) {
            var translationKey = new TranslationKey(this.key, original);
            var cachedTranslation = TranslationManager.getIfPresent(translationKey);
            if (cachedTranslation != null) {
                return cachedTranslation.translatedText();
            } else {
                TranslationManager.addTranslationTask(new TranslationTask(translationKey, (result) -> {
                    if (result != null) {
                        try {
                            var builder = ImmutableList.<FormattedText>builder();
                            this.decomposeTemplate(result.translatedText(), builder::add);
                            this.decomposedParts = builder.build();
                        } catch (TranslatableFormatException translatableformatexception) {
                            this.decomposedParts = ImmutableList.of(FormattedText.of(result.translatedText()));
                        }
                    }
                }));
            }
        }
        return original;
    }

    @ModifyExpressionValue(
            method = "decompose()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/locale/Language;getOrDefault(Ljava/lang/String;)Ljava/lang/String;",
                    ordinal = 0
            )
    )
    private String modifyWithoutFallback(String original) {
        if (!TranslationManager.isReady() || WorldLoadStateManager.isWorldLoading()) {
            return original;
        }
        // No translation needed in the following cases:
        // 1. The key already has localized content in current language, indicating the text is already localized
        // 2. The text is identical to the key, indicating missing localization in default language
        var needTranslate = !I18nManager.hasInCurrentLanguage(this.key) && !this.key.equals(original);
        if (needTranslate && Utils.hasTranslatableText(original)) {
            var translationKey = new TranslationKey(this.key, original);
            var cachedTranslation = TranslationManager.getIfPresent(translationKey);
            if (cachedTranslation != null) {
                return cachedTranslation.translatedText();
            } else {
                TranslationManager.addTranslationTask(new TranslationTask(translationKey, (result) -> {
                    if (result != null) {
                        try {
                            var builder = ImmutableList.<FormattedText>builder();
                            this.decomposeTemplate(result.translatedText(), builder::add);
                            this.decomposedParts = builder.build();
                        } catch (TranslatableFormatException translatableformatexception) {
                            this.decomposedParts = ImmutableList.of(FormattedText.of(result.translatedText()));
                        }
                    }
                }));
            }
        }
        return original;
    }
}
