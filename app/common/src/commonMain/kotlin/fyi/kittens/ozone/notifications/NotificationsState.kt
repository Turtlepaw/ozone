package fyi.kittens.ozone.notifications

import fyi.kittens.ozone.error.ErrorProps
import fyi.kittens.ozone.model.Notifications

sealed interface NotificationsState {
  val notifications: Notifications

  data class ShowingNotifications(
    override val notifications: Notifications,
    val isLoading: Boolean,
  ) : NotificationsState

  data class ShowingError(
    override val notifications: Notifications,
    val props: ErrorProps,
  ) : NotificationsState
}
