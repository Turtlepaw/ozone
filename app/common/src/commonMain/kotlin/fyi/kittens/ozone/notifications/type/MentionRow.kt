package fyi.kittens.ozone.notifications.type

import androidx.compose.runtime.Composable
import fyi.kittens.ozone.compose.asReplyInfo
import fyi.kittens.ozone.model.Notification
import fyi.kittens.ozone.notifications.NotificationRowContext
import fyi.kittens.ozone.timeline.components.TimelinePostItem

@Composable
fun MentionRow(
  context: NotificationRowContext,
  content: Notification.Content.Mentioned,
) {
  TimelinePostItem(
    now = context.now,
    post = content.post,
    onOpenPost = context.onOpenPost,
    onOpenUser = context.onOpenUser,
    onOpenImage = context.onOpenImage,
    onReplyToPost = { context.onReplyToPost(content.post.asReplyInfo()) },
  )
}
