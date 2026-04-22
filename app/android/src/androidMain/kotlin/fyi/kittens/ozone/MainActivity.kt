package fyi.kittens.ozone

import android.graphics.Color
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import dev.marcellogalhardo.retained.activity.retain
import fyi.kittens.ozone.app.initWorkflow
import fyi.kittens.ozone.store.storage
import fyi.kittens.ozone.ui.AppTheme
import fyi.kittens.ozone.ui.workflow.WorkflowRendering

class MainActivity : AppCompatActivity() {
  private val workflow by retain { initWorkflow(lifecycleScope, storage) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    window?.statusBarColor = Color.TRANSPARENT
    WindowCompat.setDecorFitsSystemWindows(window, false)

    setContent {
      AppTheme {
        StatusBarTheme()
        WorkflowRendering(
          workflow = workflow,
          onOutput = { finish() },
          content = { it.Content() },
        )
      }
    }
  }
}
