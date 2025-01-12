package com.sihenzhang.autotranslator;

public class WorldLoadStateManager {
    private static boolean WORLD_LOADING = false;
    private static boolean RECIPES_UPDATED = false;
    private static boolean TAGS_UPDATED = false;
    private static boolean PLAYER_LOGGED_IN = false;

    public static void setWorldLoading(boolean worldLoading) {
        WORLD_LOADING = worldLoading;
    }

    public static void setRecipesUpdated() {
        RECIPES_UPDATED = true;
    }

    public static void setTagsUpdated() {
        TAGS_UPDATED = true;
    }

    public static void setPlayerLoggedIn(boolean playerLoggedIn) {
        PLAYER_LOGGED_IN = playerLoggedIn;
    }

    public static boolean isWorldLoading() {
        return WORLD_LOADING;
    }

    public static void resetWorldLoadState() {
        if (WORLD_LOADING && RECIPES_UPDATED && TAGS_UPDATED && PLAYER_LOGGED_IN) {
            WORLD_LOADING = false;
            RECIPES_UPDATED = false;
            TAGS_UPDATED = false;
        }
    }
}
