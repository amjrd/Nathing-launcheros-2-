package com.example.service

import android.content.Context
import com.example.model.LauncherSettings
import com.example.model.NosWidgetPortType
import org.json.JSONArray
import org.json.JSONObject

/**
 * Small, dependency-free persistence layer for launcher preferences.
 * Settings are tiny, so SharedPreferences keeps this fast and avoids background work.
 */
object LauncherSettingsStore {
  private const val PREFS = "nothing_os_launcher_settings"
  private const val KEY_SETTINGS = "settings_original_os5_v2"

  fun load(context: Context): LauncherSettings {
    val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
      .getString(KEY_SETTINGS, null) ?: return LauncherSettings()

    return try {
      val o = JSONObject(raw)
      val defaults = LauncherSettings()
      val lockDefaults = defaults.lockScreen

      defaults.copy(
        iconPack = enumOr(o, "iconPack", defaults.iconPack),
        themeMode = enumOr(o, "themeMode", defaults.themeMode),
        clockStyle = enumOr(o, "clockStyle", defaults.clockStyle),
        accentColorIndex = o.optInt("accentColorIndex", defaults.accentColorIndex),
        gridColumns = o.optInt("gridColumns", defaults.gridColumns).coerceIn(4, 5),
        showLabels = o.optBoolean("showLabels", defaults.showLabels),
        is12HourFormat = o.optBoolean("is12HourFormat", defaults.is12HourFormat),
        tempUnitCelsius = o.optBoolean("tempUnitCelsius", defaults.tempUnitCelsius),
        doubleTapToSleep = o.optBoolean("doubleTapToSleep", defaults.doubleTapToSleep),
        swipeDownNotifications = o.optBoolean("swipeDownNotifications", defaults.swipeDownNotifications),
        showSearchBarOnDock = o.optBoolean("showSearchBarOnDock", defaults.showSearchBarOnDock),
        hapticFeedbackEnabled = o.optBoolean("hapticFeedbackEnabled", defaults.hapticFeedbackEnabled),
        wallpaperIndex = o.optInt("wallpaperIndex", defaults.wallpaperIndex),
        customWallpaperUri = o.optString("customWallpaperUri", null),
        lockScreenWallpaperIndex = o.optInt("lockScreenWallpaperIndex", defaults.lockScreenWallpaperIndex),
        customLockScreenWallpaperUri = o.optString("customLockScreenWallpaperUri", null),
        wallpaperDimPct = o.optInt("wallpaperDimPct", defaults.wallpaperDimPct).coerceIn(0, 70),
        lockScreen = lockDefaults.copy(
          isLockScreenEnabled = o.optBoolean("lockEnabled", lockDefaults.isLockScreenEnabled),
          preventSystemLockOverlap = o.optBoolean("preventSystemLockOverlap", lockDefaults.preventSystemLockOverlap),
          securityType = enumOr(o, "securityType", lockDefaults.securityType),
          pinCode = o.optString("pinCode", lockDefaults.pinCode)
            .filter(Char::isDigit).take(4).ifEmpty { lockDefaults.pinCode },
          clockStyle = enumOr(o, "lockClockStyle", lockDefaults.clockStyle),
          showWidgets = o.optBoolean("lockShowWidgets", lockDefaults.showWidgets),
          showNotifications = o.optBoolean("lockShowNotifications", lockDefaults.showNotifications),
          showBatteryGlyph = o.optBoolean("lockShowBatteryGlyph", lockDefaults.showBatteryGlyph),
          leftShortcut = enumOr(o, "leftShortcut", lockDefaults.leftShortcut),
          rightShortcut = enumOr(o, "rightShortcut", lockDefaults.rightShortcut),
          customOwnerInfo = o.optString("customOwnerInfo", lockDefaults.customOwnerInfo)
        ),
        activeWidgets = loadWidgets(o, defaults.activeWidgets)
      )
    } catch (_: Exception) {
      LauncherSettings()
    }
  }

  fun save(context: Context, settings: LauncherSettings) {
    val lock = settings.lockScreen
    val o = JSONObject().apply {
      put("iconPack", settings.iconPack.name)
      put("themeMode", settings.themeMode.name)
      put("clockStyle", settings.clockStyle.name)
      put("accentColorIndex", settings.accentColorIndex)
      put("gridColumns", settings.gridColumns)
      put("showLabels", settings.showLabels)
      put("is12HourFormat", settings.is12HourFormat)
      put("tempUnitCelsius", settings.tempUnitCelsius)
      put("doubleTapToSleep", settings.doubleTapToSleep)
      put("swipeDownNotifications", settings.swipeDownNotifications)
      put("showSearchBarOnDock", settings.showSearchBarOnDock)
      put("hapticFeedbackEnabled", settings.hapticFeedbackEnabled)
      put("widgetScales", JSONObject(settings.widgetScales.mapValues { (_, v) -> v.toDouble() }))
      put("wallpaperIndex", settings.wallpaperIndex)
      put("customWallpaperUri", settings.customWallpaperUri)
      put("lockScreenWallpaperIndex", settings.lockScreenWallpaperIndex)
      put("customLockScreenWallpaperUri", settings.customLockScreenWallpaperUri)
      put("wallpaperDimPct", settings.wallpaperDimPct)
      put("lockEnabled", lock.isLockScreenEnabled)
      put("preventSystemLockOverlap", lock.preventSystemLockOverlap)
      put("securityType", lock.securityType.name)
      put("pinCode", lock.pinCode)
      put("lockClockStyle", lock.clockStyle.name)
      put("lockShowWidgets", lock.showWidgets)
      put("lockShowNotifications", lock.showNotifications)
      put("lockShowBatteryGlyph", lock.showBatteryGlyph)
      put("leftShortcut", lock.leftShortcut.name)
      put("rightShortcut", lock.rightShortcut.name)
      put("customOwnerInfo", lock.customOwnerInfo)
      put("activeWidgets", JSONArray(settings.activeWidgets.map { it.name }))
    }

    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
      .edit()
      .putString(KEY_SETTINGS, o.toString())
      .apply()
  }

  private fun loadWidgetScales(o: JSONObject): Map<String, Float> {
    val scales = o.optJSONObject("widgetScales") ?: return emptyMap()
    val result = mutableMapOf<String, Float>()
    val keys = scales.keys()
    while (keys.hasNext()) {
      val key = keys.next()
      val value = scales.optDouble(key, 1.0).toFloat().coerceIn(0.75f, 1.5f)
      result[key] = value
    }
    return result
  }

  private inline fun <reified T : Enum<T>> enumOr(
    o: JSONObject,
    key: String,
    fallback: T
  ): T {
    val value = o.optString(key, fallback.name)
    return try { enumValueOf<T>(value) } catch (_: Exception) { fallback }
  }

  private fun loadWidgets(
    o: JSONObject,
    fallback: List<NosWidgetPortType>
  ): List<NosWidgetPortType> {
    val array = o.optJSONArray("activeWidgets") ?: return fallback
    return buildList {
      for (i in 0 until array.length()) {
        try {
          add(NosWidgetPortType.valueOf(array.optString(i)))
        } catch (_: Exception) {
          // Ignore removed/unknown widget types from older versions.
        }
      }
    }
  }
}