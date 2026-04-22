package fyi.kittens.ozone.login.auth

import kotlinx.serialization.Serializable
import fyi.kittens.ozone.api.Did
import fyi.kittens.ozone.api.Handle

@Serializable
data class AuthInfo(
  val accessJwt: String,
  val refreshJwt: String,
  val handle: Handle,
  val did: Did,
)
