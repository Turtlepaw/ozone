package fyi.kittens.ozone.app

import fyi.kittens.ozone.home.HomeProps

sealed interface AppState {
  object ShowingLogin : AppState

  data class ShowingLoggedIn(
    val props: HomeProps,
  ) : AppState
}
