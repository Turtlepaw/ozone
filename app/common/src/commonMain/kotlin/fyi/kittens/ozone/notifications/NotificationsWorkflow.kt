package fyi.kittens.ozone.notifications

import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.action
import com.squareup.workflow1.asWorker
import com.squareup.workflow1.runningWorker
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.delay
import kotlin.time.Clock
import me.tatarka.inject.annotations.Inject
import fyi.kittens.ozone.app.AppScreen
import fyi.kittens.ozone.compose.ComposePostProps
import fyi.kittens.ozone.error.ErrorOutput
import fyi.kittens.ozone.error.ErrorWorkflow
import fyi.kittens.ozone.home.HomeSubDestination
import fyi.kittens.ozone.model.Moment
import fyi.kittens.ozone.model.Notifications
import fyi.kittens.ozone.notifications.NotificationsOutput.CloseApp
import fyi.kittens.ozone.notifications.NotificationsOutput.EnterScreen
import fyi.kittens.ozone.notifications.NotificationsState.ShowingError
import fyi.kittens.ozone.notifications.NotificationsState.ShowingNotifications
import fyi.kittens.ozone.profile.ProfileProps
import kotlin.time.Duration.Companion.seconds

@Inject
class NotificationsWorkflow(
  private val clock: Clock,
  private val notificationsRepository: NotificationsRepository,
  private val errorWorkflow: ErrorWorkflow,
) : StatefulWorkflow<Unit, NotificationsState, NotificationsOutput, AppScreen>() {
  override fun initialState(
    props: Unit,
    snapshot: Snapshot?,
  ): NotificationsState = ShowingNotifications(
    notifications = Notifications(persistentListOf(), null),
    isLoading = true,
  )

  override fun render(
    renderProps: Unit,
    renderState: NotificationsState,
    context: RenderContext,
  ): AppScreen {
    context.runningSideEffect("prevent-notification-refresh") {
      notificationsRepository.doNotRefreshWhileActive()
    }

    context.runningWorker(notificationsRepository.notifications.asWorker()) { notifications ->
      action {
        state = ShowingNotifications(notifications, isLoading = false)
      }
    }
    context.runningWorker(notificationsRepository.errors.asWorker()) { error ->
      action {
        state = ShowingError(state.notifications, error)
      }
    }

    val notificationsScreen = context.notificationsScreen(renderState.notifications)

    return when (renderState) {
      is ShowingNotifications -> {
        renderState.notifications.list.maxOfOrNull { it.indexedAt }?.let { lastSeenNotification ->
          context.runningSideEffect("last-notification-seen-$lastSeenNotification") {
            val now = clock.now()
            delay(2.seconds)
            notificationsRepository.updateSeenAt(now)
          }
        }

        if (renderState.isLoading) {
          context.runningSideEffect("load-notifications") {
            if (renderState.notifications.list.isEmpty()) {
              notificationsRepository.refresh()
            } else {
              notificationsRepository.loadMore()
            }
          }
        }

        AppScreen(main = notificationsScreen)
      }
      is ShowingError -> {
        AppScreen(
          main = notificationsScreen,
          overlay = context.renderChild(errorWorkflow, renderState.props) { output ->
            action {
              state = when (output) {
                ErrorOutput.Dismiss -> ShowingNotifications(state.notifications, isLoading = false)
                ErrorOutput.Retry -> ShowingNotifications(state.notifications, isLoading = true)
              }
            }
          }
        )
      }
    }
  }

  override fun snapshotState(state: NotificationsState): Snapshot? = null

  private fun RenderContext.notificationsScreen(
    notifications: Notifications,
  ): NotificationsScreen {
    return NotificationsScreen(
      now = Moment(clock.now()),
      notifications = notifications.list,
      onLoadMore = eventHandler {
        state = ShowingNotifications(
          notifications = state.notifications,
          isLoading = true,
        )
      },
      onExit = eventHandler {
        setOutput(CloseApp)
      },
      onOpenPost = eventHandler { props ->
        setOutput(EnterScreen(HomeSubDestination.GoToThread(props)))
      },
      onOpenUser = eventHandler { user ->
        setOutput(EnterScreen(HomeSubDestination.GoToProfile(ProfileProps(user))))
      },
      onOpenImage = eventHandler { _ ->
        // No-op
      },
      onReplyToPost = eventHandler { postInfo ->
        val props = ComposePostProps(replyTo = postInfo)
        setOutput(EnterScreen(HomeSubDestination.GoToComposePost(props)))
      },
    )
  }
}
