package fyi.kittens.ozone.timeline

import fyi.kittens.ozone.home.HomeSubDestination

sealed interface TimelineOutput {
  data class EnterScreen(
    val dest: HomeSubDestination,
  ) : TimelineOutput

  object CloseApp : TimelineOutput
}
