package fyi.kittens.ozone.util

import androidx.compose.runtime.Composable
import fyi.kittens.ozone.model.Moment

@Composable
expect fun Moment.formatDate(): String

@Composable
expect fun Moment.formatTime(): String
