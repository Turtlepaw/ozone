package fyi.kittens.ozone.notifications

import fyi.kittens.ozone.home.HomeSubDestination

sealed interface NotificationsOutput {
  data class EnterScreen(
    val dest: HomeSubDestination,
  ) : NotificationsOutput

  object CloseApp : NotificationsOutput
}
