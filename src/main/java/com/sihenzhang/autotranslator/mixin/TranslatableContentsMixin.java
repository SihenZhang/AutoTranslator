package com.sihenzhang.autotranslator.mixin;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.sihenzhang.autotranslator.I18nManager;
import com.sihenzhang.autotranslator.TranslationManager;
import com.sihenzhang.autotranslator.Utils;
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
        // 以下情况下无需翻译：
        // 1. 当前语言下已经有该 key 对应的本地化内容，表明该文本就是本地化内容
        // 2. 该文本是 fallback 值
        // 3. 该文本和 key 完全相同，表明缺少默认语言下的本地化内容
        boolean needTranslate = !I18nManager.hasInCurrentLanguage(this.key) &&
                !(this.fallback != null && this.fallback.equals(original)) &&
                !this.key.equals(original);
        if (needTranslate && Utils.hasTranslatableText(original)) {
            var ifPresent = TranslationManager.CACHE.getIfPresent(this.key);
            if (ifPresent != null) {
                return ifPresent;
            } else {
                TranslationManager.translate(this.key, original).thenAccept((result) -> {
                    if (result != null) {
                        try {
                            ImmutableList.Builder<FormattedText> builder = ImmutableList.builder();
                            this.decomposeTemplate(result, builder::add);
                            this.decomposedParts = builder.build();
                        } catch (TranslatableFormatException translatableformatexception) {
                            this.decomposedParts = ImmutableList.of(FormattedText.of(result));
                        }
                    }
                });
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
        // 以下情况下无需翻译：
        // 1. 当前语言下已经有该 key 对应的本地化内容，表明该文本就是本地化内容
        // 2. 该文本和 key 完全相同，表明缺少默认语言下的本地化内容
        boolean needTranslate = !I18nManager.hasInCurrentLanguage(this.key) && !this.key.equals(original);
        if (needTranslate && Utils.hasTranslatableText(original)) {
            var ifPresent = TranslationManager.CACHE.getIfPresent(this.key);
            if (ifPresent != null) {
                return ifPresent;
            } else {
                TranslationManager.translate(this.key, original).thenAccept((result) -> {
                    if (result != null) {
                        try {
                            ImmutableList.Builder<FormattedText> builder = ImmutableList.builder();
                            this.decomposeTemplate(result, builder::add);
                            this.decomposedParts = builder.build();
                        } catch (TranslatableFormatException translatableformatexception) {
                            this.decomposedParts = ImmutableList.of(FormattedText.of(result));
                        }
                    }
                });
            }
        }
        return original;
    }
}
