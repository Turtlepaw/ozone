package fyi.kittens.ozone.login

import com.atproto.server.CreateAccountRequest
import com.atproto.server.CreateSessionRequest
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.action
import com.squareup.workflow1.runningWorker
import me.tatarka.inject.annotations.Inject
import fyi.kittens.ozone.api.ApiProvider
import fyi.kittens.ozone.api.NetworkWorker
import fyi.kittens.ozone.api.ServerRepository
import fyi.kittens.ozone.api.response.AtpResponse
import fyi.kittens.ozone.app.AppScreen
import fyi.kittens.ozone.error.ErrorOutput
import fyi.kittens.ozone.error.ErrorProps
import fyi.kittens.ozone.error.ErrorWorkflow
import fyi.kittens.ozone.error.toErrorProps
import fyi.kittens.ozone.login.LoginOutput.CanceledLogin
import fyi.kittens.ozone.login.LoginOutput.LoggedIn
import fyi.kittens.ozone.login.LoginState.ShowingError
import fyi.kittens.ozone.login.LoginState.ShowingLogin
import fyi.kittens.ozone.login.LoginState.SigningIn
import fyi.kittens.ozone.login.auth.AuthInfo
import fyi.kittens.ozone.login.auth.Credentials
import fyi.kittens.ozone.login.auth.Server
import fyi.kittens.ozone.login.auth.ServerInfo
import fyi.kittens.ozone.ui.compose.TextOverlayScreen
import fyi.kittens.ozone.ui.workflow.Dismissable.DismissHandler
import fyi.kittens.ozone.util.toReadOnlyList

@Inject
class LoginWorkflow(
  private val serverRepository: ServerRepository,
  private val apiProvider: ApiProvider,
  private val errorWorkflow: ErrorWorkflow,
) : StatefulWorkflow<Unit, LoginState, LoginOutput, AppScreen>() {
  override fun initialState(
    props: Unit,
    snapshot: Snapshot?,
  ): LoginState = ShowingLogin(
    mode = LoginScreenMode.SIGN_IN,
    serverInfo = null,
  )

  override fun render(
    renderProps: Unit,
    renderState: LoginState,
    context: RenderContext,
  ): AppScreen {
    val server = serverRepository.server
    context.runningWorker(serverInfo(), "server-info-${server}") { response ->
      action {
        val maybeServerInfo = response.maybeResponse()
        state = when (val currentState = state) {
          is ShowingLogin -> currentState.copy(serverInfo = maybeServerInfo)
          is SigningIn -> currentState.copy(serverInfo = maybeServerInfo)
          is ShowingError -> currentState.copy(serverInfo = maybeServerInfo)
        }
      }
    }

    val loginScreen = context.loginScreen(renderState.mode, server, renderState.serverInfo)

    return when (renderState) {
      is ShowingLogin -> {
        AppScreen(main = loginScreen)
      }
      is SigningIn -> {
        val credentials: Credentials = renderState.credentials
        context.runningWorker(signIn(renderState.mode, credentials)) { result ->
          action {
            when (result) {
              is AtpResponse.Success -> {
                setOutput(LoggedIn(result.response))
              }
              is AtpResponse.Failure -> {
                val errorProps = result.toErrorProps(true)
                  ?: ErrorProps("Oops.", "Something bad happened.", false)

                state = ShowingError(state.mode, state.serverInfo, errorProps, credentials)
              }
            }
          }
        }

        AppScreen(
          main = loginScreen,
          overlay = TextOverlayScreen(
            onDismiss = DismissHandler(context.eventHandler {
              state = ShowingLogin(mode = state.mode, serverInfo = state.serverInfo)
            }),
            text = "Signing in as ${credentials.username}...",
          )
        )
      }
      is ShowingError -> {
        AppScreen(
          main = loginScreen,
          overlay = context.renderChild(errorWorkflow, renderState.errorProps) { output ->
            action {
              state = when (output) {
                ErrorOutput.Dismiss -> ShowingLogin(
                  mode = state.mode, serverInfo = state.serverInfo
                )
                ErrorOutput.Retry -> SigningIn(
                  state.mode, state.serverInfo, renderState.credentials
                )
              }
            }
          }
        )
      }
    }
  }

  override fun snapshotState(state: LoginState): Snapshot? = null

  private fun RenderContext.loginScreen(
    mode: LoginScreenMode,
    server: Server,
    serverInfo: ServerInfo?,
  ): LoginScreen {
    return LoginScreen(
      mode = mode,
      onChangeMode = eventHandler { newMode ->
        state = when (val currentState = state) {
          is ShowingError -> currentState.copy(mode = newMode)
          is ShowingLogin -> currentState.copy(mode = newMode)
          is SigningIn -> currentState.copy(mode = newMode)
        }
      },
      server = server,
      serverInfo = serverInfo,
      onChangeServer = eventHandler { newServer ->
        serverRepository.server = newServer
      },
      onExit = eventHandler {
        setOutput(CanceledLogin)
      },
      onLogin = eventHandler { credentials ->
        state = SigningIn(state.mode, state.serverInfo, credentials)
      },
    )
  }

  private fun serverInfo(): NetworkWorker<ServerInfo> {
    return NetworkWorker {
      apiProvider.api.describeServer().map { response ->
        ServerInfo(
          inviteCodeRequired = response.inviteCodeRequired ?: false,
          availableUserDomains = response.availableUserDomains.toReadOnlyList(),
          privacyPolicy = response.links?.privacyPolicy?.uri,
          termsOfService = response.links?.termsOfService?.uri,
        )
      }
    }
  }

  private fun signIn(
    mode: LoginScreenMode,
    credentials: Credentials,
  ): NetworkWorker<AuthInfo> = NetworkWorker {
    when (mode) {
      LoginScreenMode.SIGN_UP -> {
        val request = CreateAccountRequest(
          email = credentials.email!!,
          handle = credentials.username,
          inviteCode = credentials.inviteCode,
          password = credentials.password,
          recoveryKey = null,
        )
        apiProvider.api.createAccount(request).map { response ->
          AuthInfo(
            accessJwt = response.accessJwt,
            refreshJwt = response.refreshJwt,
            handle = response.handle,
            did = response.did,
          )
        }
      }
      LoginScreenMode.SIGN_IN -> {
        val request = CreateSessionRequest(credentials.username.handle, credentials.password)
        apiProvider.api.createSession(request).map { response ->
          AuthInfo(
            accessJwt = response.accessJwt,
            refreshJwt = response.refreshJwt,
            handle = response.handle,
            did = response.did,
          )
        }
      }
    }
  }
}
