package fyi.kittens.ozone.thread

import app.bsky.feed.GetPostThreadQueryParams
import app.bsky.feed.GetPostThreadResponseThreadUnion.BlockedPost
import app.bsky.feed.GetPostThreadResponseThreadUnion.NotFoundPost
import app.bsky.feed.GetPostThreadResponseThreadUnion.ThreadViewPost
import app.bsky.feed.GetPostThreadResponseThreadUnion.Unknown
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.action
import com.squareup.workflow1.runningWorker
import kotlinx.collections.immutable.persistentListOf
import kotlin.time.Clock
import me.tatarka.inject.annotations.Inject
import fyi.kittens.ozone.api.ApiProvider
import fyi.kittens.ozone.api.AtUri
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
import fyi.kittens.ozone.error.toErrorProps
import fyi.kittens.ozone.model.Moment
import fyi.kittens.ozone.model.Thread
import fyi.kittens.ozone.model.toThread
import fyi.kittens.ozone.profile.ProfileProps
import fyi.kittens.ozone.profile.ProfileWorkflow
import fyi.kittens.ozone.thread.ThreadState.ComposingReply
import fyi.kittens.ozone.thread.ThreadState.FetchingPost
import fyi.kittens.ozone.thread.ThreadState.ShowingError
import fyi.kittens.ozone.thread.ThreadState.ShowingFullSizeImage
import fyi.kittens.ozone.thread.ThreadState.ShowingPost
import fyi.kittens.ozone.thread.ThreadState.ShowingProfile
import fyi.kittens.ozone.ui.compose.ImageOverlayScreen
import fyi.kittens.ozone.ui.compose.TextOverlayScreen
import fyi.kittens.ozone.ui.workflow.Dismissable
import fyi.kittens.ozone.ui.workflow.EmptyScreen
import fyi.kittens.ozone.util.toReadOnlyList

@Inject
class ThreadWorkflow(
  private val clock: Clock,
  private val apiProvider: ApiProvider,
  private val profileWorkflow: () -> ProfileWorkflow,
  private val composePostWorkflow: ComposePostWorkflow,
  private val errorWorkflow: ErrorWorkflow,
) : StatefulWorkflow<ThreadProps, ThreadState, Unit, AppScreen>() {
  override fun initialState(
    props: ThreadProps,
    snapshot: Snapshot?,
  ): ThreadState {
    return FetchingPost(
      thread = props.originalPost?.let { originalPost ->
        Thread(
          post = originalPost,
          parents = persistentListOf(),
          replies = persistentListOf(),
        )
      },
      previousState = null,
      uri = props.uri,
    )
  }

  override fun render(
    renderProps: ThreadProps,
    renderState: ThreadState,
    context: RenderContext
  ): AppScreen {
    val screenStack = generateSequence(renderState) { it.previousState }
      .filter { it.thread != null }
      .toList()
      .reversed()
      .map { state -> context.threadScreen(state.thread!!) }
      .ifEmpty { listOf(EmptyScreen) }
      .toReadOnlyList()

    return when (renderState) {
      is FetchingPost -> {
        context.runningWorker(loadPost(renderState.uri)) { result ->
          action {
            state = when (result) {
              is AtpResponse.Success -> {
                when (val thread = result.response.thread) {
                  is ThreadViewPost -> ShowingPost(
                    previousState = state.previousState,
                    thread = thread.value.toThread(),
                  )

                  is NotFoundPost,
                  is BlockedPost,
                  is Unknown -> ShowingError(
                    previousState = state,
                    props = ErrorProps("Oops.", "Could not load thread.", false),
                  )
                }
              }

              is AtpResponse.Failure -> {
                val errorProps = result.toErrorProps(true)
                  ?: ErrorProps("Oops.", "Could not load thread.", false)

                ShowingError(
                  previousState = state,
                  props = errorProps,
                )
              }
            }
          }
        }

        AppScreen(
          mains = screenStack,
          overlay = TextOverlayScreen(
            onDismiss = Dismissable.Ignore,
            text = "Loading thread...",
          ),
        )
      }

      is ShowingPost -> AppScreen(mains = screenStack)
      is ShowingProfile -> {
        val profileScreen = context.renderChild(profileWorkflow(), renderState.props) {
          action {
            state = renderState.previousState
          }
        }

        profileScreen.copy(mains = (screenStack + profileScreen.mains).toReadOnlyList())
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
        val profileScreen = context.renderChild(composePostWorkflow, renderState.props) { output ->
          action {
            state = when (output) {
              CanceledPost -> renderState.previousState
              CreatedPost -> FetchingPost(
                thread = state.thread,
                previousState = renderState.previousState.previousState,
                uri = props.uri,
              )
            }
          }
        }

        profileScreen.copy(mains = (screenStack + profileScreen.mains).toReadOnlyList())
      }

      is ShowingError -> {
        AppScreen(
          mains = screenStack,
          overlay = context.renderChild(errorWorkflow, renderState.props) { output ->
            action {
              when (output) {
                ErrorOutput.Dismiss -> setOutput(Unit)
                ErrorOutput.Retry -> state = renderState.previousState
              }
            }
          }
        )
      }
    }
  }

  override fun snapshotState(state: ThreadState): Snapshot? = null

  private fun RenderContext.threadScreen(thread: Thread): ThreadScreen {
    return ThreadScreen(
      now = Moment(clock.now()),
      thread = thread,
      onExit = eventHandler {
        state.previousState
          ?.let { state = it }
          ?: setOutput(Unit)
      },
      onRefresh = eventHandler {
        state = FetchingPost(
          previousState = state.previousState,
          thread = thread,
          uri = thread.post.uri,
        )
      },
      onOpenPost = eventHandler { post ->
        state = FetchingPost(
          thread = post.originalPost?.let { originalPost ->
            Thread(
              post = originalPost,
              parents = persistentListOf(),
              replies = persistentListOf(),
            )
          },
          previousState = state,
          uri = post.uri,
        )
      },
      onOpenImage = eventHandler { action ->
        state = ShowingFullSizeImage(state, action)
      },
      onOpenUser = eventHandler { user ->
        state = ShowingProfile(state, ProfileProps(user, null))
      },
      onReplyToPost = eventHandler { postInfo ->
        state = ComposingReply(state, ComposePostProps(replyTo = postInfo))
      }
    )
  }

  private fun loadPost(uri: AtUri) = NetworkWorker {
    apiProvider.api.getPostThread(GetPostThreadQueryParams(uri))
  }
}
