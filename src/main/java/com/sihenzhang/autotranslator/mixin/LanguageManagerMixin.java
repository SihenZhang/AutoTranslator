package com.sihenzhang.autotranslator.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.sihenzhang.autotranslator.I18nManager;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(LanguageManager.class)
public abstract class LanguageManagerMixin {
    @Inject(
            method = "onResourceManagerReload(Lnet/minecraft/server/packs/resources/ResourceManager;)V",
            at = @At("TAIL")
    )
    private void onResourceManagerReload(ResourceManager resourceManager, CallbackInfo ci, @Local List<String> list, @Local boolean flag) {
        I18nManager.loadFrom(resourceManager, list, flag);
    }
}
