package fyi.kittens.ozone.profile

import fyi.kittens.ozone.compose.ComposePostProps
import fyi.kittens.ozone.error.ErrorProps
import fyi.kittens.ozone.model.FullProfile
import fyi.kittens.ozone.model.Timeline
import fyi.kittens.ozone.thread.ThreadProps
import fyi.kittens.ozone.ui.compose.OpenImageAction
import fyi.kittens.ozone.user.UserReference
import fyi.kittens.ozone.util.RemoteData
import fyi.kittens.ozone.util.RemoteData.Failed

sealed interface ProfileState {
  val user: UserReference
  val profile: RemoteData<FullProfile>
  val feed: RemoteData<Timeline>
  val previousState: ProfileState?

  data class ShowingProfile(
    override val user: UserReference,
    override val profile: RemoteData<FullProfile>,
    override val feed: RemoteData<Timeline>,
    override val previousState: ProfileState?,
  ) : ProfileState

  data class ShowingFullSizeImage(
    override val previousState: ProfileState,
    val openImageAction: OpenImageAction,
  ) : ProfileState by previousState

  data class ComposingReply(
    override val previousState: ProfileState,
    val props: ComposePostProps,
  ) : ProfileState by previousState

  data class ShowingThread(
    override val previousState: ProfileState,
    val props: ThreadProps,
  ) : ProfileState by previousState

  data class ShowingError(
    override val user: UserReference,
    override val profile: RemoteData<FullProfile>,
    override val feed: RemoteData<Timeline>,
    override val previousState: ProfileState?,
  ) : ProfileState {
    val error: ErrorProps
      get() = (profile as? Failed)?.error
        ?: (feed as? Failed)?.error
        ?: error("No error found: profile=$profile, feed=$feed")
  }
}
