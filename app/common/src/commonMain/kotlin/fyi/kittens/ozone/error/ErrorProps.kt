package fyi.kittens.ozone.error

import fyi.kittens.ozone.api.response.AtpResponse

data class ErrorProps(
  val title: String?,
  val description: String?,
  val retryable: Boolean,
)

fun AtpResponse.Failure<*>.toErrorProps(retryable: Boolean): ErrorProps? =
  error?.let { ErrorProps(it.error, it.message, retryable) }
