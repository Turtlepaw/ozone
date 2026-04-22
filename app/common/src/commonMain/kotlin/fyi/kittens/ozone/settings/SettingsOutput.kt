package fyi.kittens.ozone.settings

sealed interface SettingsOutput {
  object SignOut : SettingsOutput

  object CloseApp : SettingsOutput
}
