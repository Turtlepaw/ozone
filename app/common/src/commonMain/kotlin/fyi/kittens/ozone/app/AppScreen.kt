package fyi.kittens.ozone.app

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.persistentListOf
import fyi.kittens.ozone.ui.compose.Overlay
import fyi.kittens.ozone.ui.workflow.Dismissable.DismissHandler
import fyi.kittens.ozone.ui.workflow.Dismissable.Ignore
import fyi.kittens.ozone.ui.workflow.OverlayRendering
import fyi.kittens.ozone.ui.workflow.ViewRendering
import fyi.kittens.ozone.util.ReadOnlyList

data class AppScreen(
  val mains: ReadOnlyList<ViewRendering>,
  val overlay: OverlayRendering? = null,
) : ViewRendering {
  @Composable
  override fun Content() {
    Box(Modifier.fillMaxSize()) {
      Box {
        mains.forEach { main -> main.Content() }
      }

      val overlayVisibility = remember { MutableTransitionState(false) }

      Overlay(
        modifier = Modifier.fillMaxSize(),
        visibleState = overlayVisibility,
        enter = overlay?.enter ?: EnterTransition.None,
        exit = overlay?.exit ?: ExitTransition.None,
        onClickOutside = {
          when (overlay?.onDismiss) {
            is DismissHandler -> overlayVisibility.targetState = false
            is Ignore -> Unit
            null -> Unit
          }
        },
      ) {
        overlay?.Content(onRequestDismiss = {
          when (overlay.onDismiss) {
            is DismissHandler -> overlayVisibility.targetState = false
            is Ignore -> Unit
          }
        })
      }

      LaunchedEffect(overlay) {
        overlayVisibility.targetState = overlay != null
      }

      LaunchedEffect(overlayVisibility.currentState) {
        if (!overlayVisibility.targetState && !overlayVisibility.currentState && overlay != null) {
          when (val onDismiss = overlay.onDismiss) {
            is DismissHandler -> onDismiss.handler()
            is Ignore -> Unit
          }
        }
      }
    }
  }

  constructor(
    main: ViewRendering,
    overlay: OverlayRendering? = null
  ) : this(persistentListOf(main), overlay)
}
