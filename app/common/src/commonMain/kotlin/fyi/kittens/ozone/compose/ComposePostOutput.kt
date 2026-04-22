package fyi.kittens.ozone.compose

sealed interface ComposePostOutput {
  object CreatedPost : ComposePostOutput
  object CanceledPost : ComposePostOutput
}
