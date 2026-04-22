package fyi.kittens.ozone.timeline

import fyi.kittens.ozone.error.ErrorProps
import fyi.kittens.ozone.model.FullProfile
import fyi.kittens.ozone.model.Timeline
import fyi.kittens.ozone.ui.compose.OpenImageAction

sealed interface TimelineState {
  val profile: FullProfile?
  val timeline: Timeline?

  data class FetchingTimeline(
    override val profile: FullProfile?,
    override val timeline: Timeline?,
    val fullRefresh: Boolean,
  ) : TimelineState

  data class ShowingTimeline(
    override val profile: FullProfile,
    override val timeline: Timeline,
    val showRefreshPrompt: Boolean,
  ) : TimelineState

  data class ShowingFullSizeImage(
    val previousState: TimelineState,
    val openImageAction: OpenImageAction,
  ) : TimelineState by previousState

  data class ShowingError(
    val previousState: TimelineState,
    val props: ErrorProps,
  ) : TimelineState by previousState
}
