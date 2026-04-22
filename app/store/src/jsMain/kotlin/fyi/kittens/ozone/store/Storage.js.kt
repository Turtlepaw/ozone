package fyi.kittens.ozone.store

import com.russhwolf.settings.StorageSettings
import kotlinx.browser.localStorage
import fyi.kittens.ozone.store.settings.SettingsStorage

fun storage(): PersistentStorage {
  val settings = StorageSettings(delegate = localStorage)
  return SettingsStorage(settings)
}
