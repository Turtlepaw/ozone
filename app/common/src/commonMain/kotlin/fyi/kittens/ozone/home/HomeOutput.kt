package fyi.kittens.ozone.home

sealed interface HomeOutput {
  object SignOut : HomeOutput
  object CloseApp : HomeOutput
}
