package fyi.kittens.ozone

import androidx.compose.foundation.layout.Box
import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import platform.Foundation.NSSelectorFromString
import platform.UIKit.UIApplication
import fyi.kittens.ozone.api.OzoneDispatchers.IO
import fyi.kittens.ozone.app.AppWorkflow
import fyi.kittens.ozone.app.initWorkflow
import fyi.kittens.ozone.store.storage
import fyi.kittens.ozone.ui.AppTheme
import fyi.kittens.ozone.ui.workflow.WorkflowRendering

lateinit var workflow: AppWorkflow

@Suppress("unused") // Called from iOS application code.
fun initialize() {
  workflow = initWorkflow(CoroutineScope(IO), storage())
}

@OptIn(ExperimentalForeignApi::class)
@Suppress("unused", "FunctionName") // Called from iOS application code.
fun MainViewController() = ComposeUIViewController {
  Box {
    AppTheme {
      WorkflowRendering(
        workflow = workflow,
        onOutput = {
          UIApplication.sharedApplication.performSelector(
            aSelector = NSSelectorFromString("suspend"),
            withObject = null,
          )
        },
        content = { it.Content() },
      )
    }
  }
}
