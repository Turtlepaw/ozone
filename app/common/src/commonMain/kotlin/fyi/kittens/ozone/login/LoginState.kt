package fyi.kittens.ozone.login

import fyi.kittens.ozone.error.ErrorProps
import fyi.kittens.ozone.login.auth.Credentials
import fyi.kittens.ozone.login.auth.ServerInfo

sealed interface LoginState {
  val mode: LoginScreenMode
  val serverInfo: ServerInfo?

  data class ShowingLogin(
    override val mode: LoginScreenMode,
    override val serverInfo: ServerInfo?,
  ) : LoginState

  data class SigningIn(
    override val mode: LoginScreenMode,
    override val serverInfo: ServerInfo?,
    val credentials: Credentials,
  ) : LoginState

  data class ShowingError(
    override val mode: LoginScreenMode,
    override val serverInfo: ServerInfo?,
    val errorProps: ErrorProps,
    val credentials: Credentials,
  ) : LoginState
}
