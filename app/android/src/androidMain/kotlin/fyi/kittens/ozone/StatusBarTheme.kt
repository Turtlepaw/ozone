package fyi.kittens.ozone

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import fyi.kittens.ozone.ui.LocalColorTheme

@Composable
fun StatusBarTheme() {
  val window = (LocalContext.current as Activity).window
  val view = window.decorView

  if (!view.isInEditMode) {
    val lightTheme = LocalColorTheme.current.isLight()

    SideEffect {
      WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = lightTheme
    }
  }
}
