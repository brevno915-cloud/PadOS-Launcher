package com.padlauncher

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable?
)

object LauncherPreferences {
    private const val PREFS = "pados_launcher_prefs"
    private const val KEY_GRID_COLUMNS = "grid_columns"
    private const val KEY_ICON_SIZE = "icon_size"
    private const val KEY_DOCK_ITEMS = "dock_items"
    private const val KEY_WALLPAPER_URI = "wallpaper_uri"
    private const val KEY_THEME = "theme"
    private const val KEY_BRIGHTNESS = "brightness"
    private const val KEY_ANIMATION = "animation"

    fun getGridColumns(prefs: android.content.SharedPreferences): Int =
        prefs.getInt(KEY_GRID_COLUMNS, 4)

    fun setGridColumns(prefs: android.content.SharedPreferences, value: Int) =
        prefs.edit().putInt(KEY_GRID_COLUMNS, value.coerceIn(3, 6)).apply()

    fun getIconSize(prefs: android.content.SharedPreferences): Int =
        prefs.getInt(KEY_ICON_SIZE, 64)

    fun setIconSize(prefs: android.content.SharedPreferences, value: Int) =
        prefs.edit().putInt(KEY_ICON_SIZE, value.coerceIn(48, 96)).apply()

    fun getDockItems(prefs: android.content.SharedPreferences): Set<String> =
        prefs.getStringSet(KEY_DOCK_ITEMS, emptySet()) ?: emptySet()

    fun setDockItems(prefs: android.content.SharedPreferences, items: Set<String>) =
        prefs.edit().putStringSet(KEY_DOCK_ITEMS, items).apply()

    fun getWallpaperUri(prefs: android.content.SharedPreferences): String? =
        prefs.getString(KEY_WALLPAPER_URI, null)

    fun setWallpaperUri(prefs: android.content.SharedPreferences, uri: String?) =
        prefs.edit().putString(KEY_WALLPAPER_URI, uri).apply()

    fun getTheme(prefs: android.content.SharedPreferences): String =
        prefs.getString(KEY_THEME, "dark") ?: "dark"

    fun setTheme(prefs: android.content.SharedPreferences, value: String) =
        prefs.edit().putString(KEY_THEME, value).apply()

    fun getBrightness(prefs: android.content.SharedPreferences): Int =
        prefs.getInt(KEY_BRIGHTNESS, 70)

    fun setBrightness(prefs: android.content.SharedPreferences, value: Int) =
        prefs.edit().putInt(KEY_BRIGHTNESS, value.coerceIn(0, 100)).apply()

    fun getAnimationEnabled(prefs: android.content.SharedPreferences): Boolean =
        prefs.getBoolean(KEY_ANIMATION, true)

    fun setAnimationEnabled(prefs: android.content.SharedPreferences, value: Boolean) =
        prefs.edit().putBoolean(KEY_ANIMATION, value).apply()

    fun getSharedPreferences(context: android.content.Context): android.content.SharedPreferences =
        context.getSharedPreferences(PREFS, android.content.Context.MODE_PRIVATE)
}

object AppLoader {
    fun loadInstalledApps(context: android.content.Context): List<AppInfo> {
        val pm = context.packageManager
        val launchableApps = mutableListOf<AppInfo>()

        try {
            val installed = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            for (app in installed) {
                val intent = pm.getLaunchIntentForPackage(app.packageName) ?: continue
                if (intent.action == android.content.Intent.ACTION_MAIN) {
                    val label = app.loadLabel(pm).toString()
                    val icon = app.loadIcon(pm)
                    launchableApps += AppInfo(app.packageName, label, icon)
                }
            }
        } catch (e: SecurityException) {
            return emptyList()
        }

        return launchableApps.sortedBy { it.appName.lowercase() }
    }
}
