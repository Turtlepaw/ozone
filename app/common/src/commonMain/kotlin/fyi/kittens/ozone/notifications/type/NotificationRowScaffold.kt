package fyi.kittens.ozone.notifications.type

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fyi.kittens.ozone.model.Profile
import fyi.kittens.ozone.notifications.NotificationRowContext
import fyi.kittens.ozone.ui.compose.AvatarImage
import fyi.kittens.ozone.user.UserDid
import fyi.kittens.ozone.util.color

@Composable
fun NotificationRowScaffold(
  modifier: Modifier = Modifier,
  context: NotificationRowContext,
  profile: Profile,
  icon: @Composable () -> Unit,
  content: @Composable () -> Unit,
) {
  Row(
    modifier = modifier.padding(16.dp),
    horizontalArrangement = spacedBy(16.dp),
  ) {
    Box(
      modifier = Modifier.width(48.dp),
      contentAlignment = Alignment.TopEnd,
    ) {
      icon()
    }

    Column(verticalArrangement = spacedBy(8.dp)) {
      AvatarImage(
        modifier = Modifier.size(32.dp),
        avatarUrl = profile.avatar,
        onClick = { profile.did.let { context.onOpenUser(UserDid(it)) } },
        contentDescription = profile.displayName ?: profile.handle.handle,
        fallbackColor = profile.handle.color(),
      )

      Box {
        content()
      }
    }
  }
}
