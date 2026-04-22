package fyi.kittens.ozone.timeline.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import fyi.kittens.ozone.model.Moment
import fyi.kittens.ozone.util.formatDate
import fyi.kittens.ozone.util.formatTime

@Composable
fun PostDate(time: Moment) {
  Text(
    text = "${time.formatDate()} • ${time.formatTime()}",
  )
}
