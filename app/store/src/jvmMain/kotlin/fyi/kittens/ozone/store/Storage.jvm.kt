package fyi.kittens.ozone.store

import com.russhwolf.settings.PreferencesSettings
import fyi.kittens.ozone.store.settings.SettingsStorage
import java.util.prefs.Preferences

fun storage(): PersistentStorage {
  val preferences = Preferences.userRoot().node("fyi.kittens.ozone").node("1").apply { sync() }
  val settings = PreferencesSettings(preferences)
  return SettingsStorage(settings)
}
