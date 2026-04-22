package fyi.kittens.ozone.thread

import fyi.kittens.ozone.api.AtUri
import fyi.kittens.ozone.compose.ComposePostProps
import fyi.kittens.ozone.error.ErrorProps
import fyi.kittens.ozone.model.Thread
import fyi.kittens.ozone.profile.ProfileProps
import fyi.kittens.ozone.ui.compose.OpenImageAction

sealed interface ThreadState {
  val thread: Thread?
  val previousState: ThreadState?

  data class FetchingPost(
    override val thread: Thread?,
    override val previousState: ThreadState?,
    val uri: AtUri,
  ) : ThreadState

  data class ShowingPost(
    override val thread: Thread,
    override val previousState: ThreadState?,
  ) : ThreadState

  data class ShowingProfile(
    override val previousState: ThreadState,
    val props: ProfileProps,
  ) : ThreadState by previousState

  data class ShowingFullSizeImage(
    override val previousState: ThreadState,
    val openImageAction: OpenImageAction,
  ) : ThreadState by previousState

  data class ComposingReply(
    override val previousState: ThreadState,
    val props: ComposePostProps,
  ) : ThreadState by previousState

  data class ShowingError(
    override val previousState: ThreadState,
    val props: ErrorProps,
  ) : ThreadState by previousState
}
