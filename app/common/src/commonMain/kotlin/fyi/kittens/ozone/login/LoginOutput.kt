package fyi.kittens.ozone.login

import fyi.kittens.ozone.login.auth.AuthInfo

sealed interface LoginOutput {
  object CanceledLogin : LoginOutput

  data class LoggedIn(
    val authInfo: AuthInfo,
  ) : LoginOutput
}
