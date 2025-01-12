package com.sihenzhang.autotranslator.event;

import com.sihenzhang.autotranslator.AutoTranslator;
import com.sihenzhang.autotranslator.WorldLoadStateManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RecipesUpdatedEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

@EventBusSubscriber(value = Dist.CLIENT, modid = AutoTranslator.MOD_ID)
public class WorldLoadStateEvent {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRecipesUpdated(final RecipesUpdatedEvent event) {
        WorldLoadStateManager.setRecipesUpdated();
        WorldLoadStateManager.resetWorldLoadState();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTagsUpdate(final TagsUpdatedEvent event) {
        WorldLoadStateManager.setTagsUpdated();
        WorldLoadStateManager.resetWorldLoadState();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerLoggingIn(final ClientPlayerNetworkEvent.LoggingIn event) {
        WorldLoadStateManager.setPlayerLoggedIn(true);
        WorldLoadStateManager.resetWorldLoadState();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerLoggingOut(final ClientPlayerNetworkEvent.LoggingOut event) {
        if (event.getPlayer() != null) {
            WorldLoadStateManager.setWorldLoading(false);
            WorldLoadStateManager.setPlayerLoggedIn(false);
        }
    }
}
