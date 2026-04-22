package fyi.kittens.ozone.profile

import app.bsky.feed.GetAuthorFeedQueryParams
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.Worker
import com.squareup.workflow1.WorkflowAction
import com.squareup.workflow1.action
import com.squareup.workflow1.asWorker
import com.squareup.workflow1.runningWorker
import kotlin.time.Clock
import me.tatarka.inject.annotations.Inject
import fyi.kittens.ozone.api.ApiProvider
import fyi.kittens.ozone.api.NetworkWorker
import fyi.kittens.ozone.api.response.AtpResponse
import fyi.kittens.ozone.app.AppScreen
import fyi.kittens.ozone.compose.ComposePostOutput.CanceledPost
import fyi.kittens.ozone.compose.ComposePostOutput.CreatedPost
import fyi.kittens.ozone.compose.ComposePostProps
import fyi.kittens.ozone.compose.ComposePostWorkflow
import fyi.kittens.ozone.error.ErrorOutput
import fyi.kittens.ozone.error.ErrorProps
import fyi.kittens.ozone.error.ErrorWorkflow
import fyi.kittens.ozone.model.FullProfile
import fyi.kittens.ozone.model.Moment
import fyi.kittens.ozone.model.Timeline
import fyi.kittens.ozone.model.TimelinePost
import fyi.kittens.ozone.profile.ProfileState.ComposingReply
import fyi.kittens.ozone.profile.ProfileState.ShowingError
import fyi.kittens.ozone.profile.ProfileState.ShowingFullSizeImage
import fyi.kittens.ozone.profile.ProfileState.ShowingProfile
import fyi.kittens.ozone.profile.ProfileState.ShowingThread
import fyi.kittens.ozone.thread.ThreadWorkflow
import fyi.kittens.ozone.ui.compose.ImageOverlayScreen
import fyi.kittens.ozone.ui.compose.TextOverlayScreen
import fyi.kittens.ozone.ui.workflow.Dismissable
import fyi.kittens.ozone.user.MyProfileRepository
import fyi.kittens.ozone.user.UserDatabase
import fyi.kittens.ozone.user.UserDid
import fyi.kittens.ozone.user.UserHandle
import fyi.kittens.ozone.user.UserReference
import fyi.kittens.ozone.util.RemoteData
import fyi.kittens.ozone.util.RemoteData.Failed
import fyi.kittens.ozone.util.RemoteData.Fetching
import fyi.kittens.ozone.util.RemoteData.Success
import fyi.kittens.ozone.util.toReadOnlyList

@Inject
class ProfileWorkflow(
  private val clock: Clock,
  private val apiProvider: ApiProvider,
  private val userDatabase: UserDatabase,
  private val myProfileRepository: MyProfileRepository,
  private val composePostWorkflow: ComposePostWorkflow,
  private val threadWorkflow: ThreadWorkflow,
  private val errorWorkflow: ErrorWorkflow,
) : StatefulWorkflow<ProfileProps, ProfileState, Unit, AppScreen>() {
  override fun initialState(
    props: ProfileProps,
    snapshot: Snapshot?,
  ): ProfileState = ShowingProfile(
    user = props.user,
    profile = Fetching(props.preloadedProfile),
    feed = Fetching(),
    previousState = null,
  )

  override fun render(
    renderProps: ProfileProps,
    renderState: ProfileState,
    context: RenderContext,
  ): AppScreen {
    val profileWorker = userDatabase.profile(renderState.user).asWorker()
    context.runningWorker(profileWorker, renderState.user.toString()) { result ->
      action {
        state = determineState(Success(result), state.feed)
      }
    }

    if (renderState.feed is Fetching) {
      val worker = loadPosts(renderState.user, renderState.feed.getOrNull()?.cursor)
      context.runningWorker(worker, renderState.user.toString()) { result ->
        action {
          val feedResult = RemoteData.fromAtpResponseOrError(result, state.feed) {
            ErrorProps("Oops.", "Could not load feed for @${props.user}.", true)
          }
          val combinedFeed = if (feedResult is Success) {
            val oldPosts = state.feed.getOrNull()?.posts.orEmpty()
            val newPosts = feedResult.getOrNull()?.posts.orEmpty()
            Success(
              Timeline(
                cursor = feedResult.value.cursor,
                posts = (oldPosts + newPosts).toReadOnlyList(),
              )
            )
          } else {
            feedResult
          }

          state = determineState(state.profile, combinedFeed)
        }
      }
    }

    val screenStack = generateSequence(renderState) { it.previousState }
      .toList()
      .reversed()
      .filter { it.profile is Success }
      .map { state: ProfileState ->
        context.profileScreen(
          profile = state.profile.getOrNull()!!,
          feed = state.feed.getOrNull()?.posts.orEmpty(),
        )
      }
      .toReadOnlyList()

    return when (renderState) {
      is ShowingProfile -> {
        if (renderState.profile is Fetching) {
          AppScreen(
            mains = screenStack,
            overlay = TextOverlayScreen(
              onDismiss = Dismissable.Ignore,
              text = "Loading ${renderState.user}...",
            ),
          )
        } else {
          AppScreen(mains = screenStack)
        }
      }
      is ShowingFullSizeImage -> {
        AppScreen(
          mains = screenStack,
          overlay = ImageOverlayScreen(
            onDismiss = Dismissable.DismissHandler(
              context.eventHandler { state = renderState.previousState }
            ),
            action = renderState.openImageAction,
          ),
        )
      }
      is ComposingReply -> {
        val composeScreen = context.renderChild(composePostWorkflow, renderState.props) { output ->
          action {
            state = when (output) {
              CanceledPost -> renderState.previousState
              CreatedPost -> ShowingProfile(
                user = renderState.user,
                profile = Fetching(renderState.profile.getOrNull()),
                feed = Fetching(renderState.feed.getOrNull()),
                previousState = renderState.previousState.previousState,
              )
            }
          }
        }

        composeScreen.copy(mains = (screenStack + composeScreen.mains).toReadOnlyList())
      }
      is ShowingThread -> {
        val threadScreen = context.renderChild(threadWorkflow, renderState.props) {
          action {
            state = renderState.previousState
          }
        }

        threadScreen.copy(mains = (screenStack + threadScreen.mains).toReadOnlyList())
      }
      is ShowingError -> {
        AppScreen(
          mains = screenStack,
          overlay = context.renderChild(errorWorkflow, renderState.error) { output ->
            action {
              val currentProfile = state.profile
              val currentFeed = state.feed
              when (output) {
                ErrorOutput.Dismiss -> {
                  setOutput(Unit)
                }
                ErrorOutput.Retry -> {
                  val newProfile = when (currentProfile) {
                    is Fetching -> currentProfile
                    is Success -> currentProfile
                    is Failed -> Fetching(currentProfile.previous)
                  }
                  val newFeed = when (currentFeed) {
                    is Fetching -> currentFeed
                    is Success -> currentFeed
                    is Failed -> Fetching(currentFeed.previous)
                  }
                  state = ShowingProfile(state.user, newProfile, newFeed, state.previousState)
                }
              }
            }
          }
        )
      }
    }
  }

  override fun snapshotState(state: ProfileState): Snapshot? = null

  private fun RenderContext.profileScreen(
    profile: FullProfile,
    feed: List<TimelinePost>,
  ): ProfileScreen {
    return ProfileScreen(
      now = Moment(clock.now()),
      profile = profile,
      feed = feed.toReadOnlyList(),
      isSelf = myProfileRepository.isMe(UserDid(profile.did)),
      onLoadMore = eventHandler {
        state = ShowingProfile(
          user = state.user,
          profile = state.profile,
          feed = Fetching(state.feed.getOrNull()),
          previousState = state.previousState,
        )
      },
      onOpenPost = eventHandler { props ->
        state = ShowingThread(
          previousState = state,
          props = props,
        )
      },
      onOpenUser = eventHandler { user ->
        if (user != state.user) {
          state = ShowingProfile(
            user = user,
            profile = Fetching(),
            feed = Fetching(),
            previousState = state,
          )
        }
      },
      onOpenImage = eventHandler { action ->
        state = ShowingFullSizeImage(state, action)
      },
      onReplyToPost = eventHandler { postInfo ->
        state = ComposingReply(state, ComposePostProps(replyTo = postInfo))
      },
      onExit = eventHandler {
        state.previousState
          ?.let { state = it }
          ?: setOutput(Unit)
      },
    )
  }

  private fun WorkflowAction<ProfileProps, ProfileState, Unit>.Updater.determineState(
    profile: RemoteData<FullProfile>,
    feed: RemoteData<Timeline>,
  ): ProfileState {
    return if (profile !is Failed && feed !is Failed) {
      ShowingProfile(state.user, profile, feed, state.previousState)
    } else {
      ShowingError(state.user, profile, feed, state.previousState)
    }
  }

  private fun loadPosts(
    user: UserReference,
    cursor: String?,
  ): Worker<AtpResponse<Timeline>> = NetworkWorker {
    val identifier = when (user) {
      is UserDid -> user.did
      is UserHandle -> user.handle
    }
    apiProvider.api.getAuthorFeed(
      GetAuthorFeedQueryParams(
        actor = identifier,
        limit = 100,
        cursor = cursor,
      )
    ).map { Timeline.from(it.feed, it.cursor) }
  }
}
