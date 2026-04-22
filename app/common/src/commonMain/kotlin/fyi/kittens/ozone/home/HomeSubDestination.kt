package fyi.kittens.ozone.home

import fyi.kittens.ozone.compose.ComposePostProps
import fyi.kittens.ozone.profile.ProfileProps
import fyi.kittens.ozone.thread.ThreadProps

sealed interface HomeSubDestination {
  data class GoToProfile(
    val props: ProfileProps,
  ) : HomeSubDestination

  data class GoToThread(
    val props: ThreadProps,
  ) : HomeSubDestination

  data class GoToComposePost(
    val props: ComposePostProps,
  ) : HomeSubDestination
}
