package fyi.kittens.ozone.settings

import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import me.tatarka.inject.annotations.Inject
import fyi.kittens.ozone.app.AppScreen
import fyi.kittens.ozone.settings.SettingsOutput.CloseApp
import fyi.kittens.ozone.settings.SettingsOutput.SignOut
import fyi.kittens.ozone.settings.SettingsState.ConfirmSignOut
import fyi.kittens.ozone.settings.SettingsState.ShowingSettings
import fyi.kittens.ozone.ui.workflow.ConfirmRendering

@Inject
class SettingsWorkflow : StatefulWorkflow<Unit, SettingsState, SettingsOutput, AppScreen>() {
  override fun initialState(
    props: Unit,
    snapshot: Snapshot?
  ): SettingsState = ShowingSettings

  override fun render(
    renderProps: Unit,
    renderState: SettingsState,
    context: RenderContext
  ): AppScreen {
    val settingsScreen = context.settingsScreen()
    val overlay = when (renderState) {
      is ShowingSettings -> null
      is ConfirmSignOut -> context.confirmScreen()
    }

    return AppScreen(
      main = settingsScreen,
      overlay = overlay,
    )
  }

  override fun snapshotState(state: SettingsState): Snapshot? = null

  private fun RenderContext.settingsScreen(): SettingsScreen {
    return SettingsScreen(
      onExit = eventHandler {
        setOutput(CloseApp)
      },
      onSignOut = eventHandler {
        state = ConfirmSignOut
      },
    )
  }

  private fun RenderContext.confirmScreen(): ConfirmRendering {
    return ConfirmRendering(
      title = "Sign Out?",
      description = "Your login credentials will not be saved on this device.",
      onDismiss = eventHandler {
        state = ShowingSettings
      },
      onConfirm = eventHandler {
        setOutput(SignOut)
      },
    )
  }
}
