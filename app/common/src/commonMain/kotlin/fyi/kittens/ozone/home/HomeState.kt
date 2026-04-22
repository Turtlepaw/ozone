package fyi.kittens.ozone.home

import fyi.kittens.ozone.compose.ComposePostProps
import fyi.kittens.ozone.profile.ProfileProps
import fyi.kittens.ozone.thread.ThreadProps
import fyi.kittens.ozone.timeline.TimelineProps

sealed interface HomeState {

  sealed interface InTab : HomeState {
    data class InTimeline(
      val props: TimelineProps,
    ) : InTab

    object InNotifications : InTab

    object InSettings : InTab
  }

  sealed interface InSubScreen : HomeState {
    val inTabState: InTab

    data class InProfile(
      val props: ProfileProps,
      override val inTabState: InTab,
    ) : InSubScreen

    data class InThread(
      val props: ThreadProps,
      override val inTabState: InTab,
    ) : InSubScreen

    data class InComposePost(
      val props: ComposePostProps,
      override val inTabState: InTab,
    ) : InSubScreen
  }
}
