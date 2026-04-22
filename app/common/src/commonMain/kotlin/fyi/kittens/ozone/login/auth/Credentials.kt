package fyi.kittens.ozone.login.auth

import fyi.kittens.ozone.api.Handle

data class Credentials(
  val email: String?,
  val username: Handle,
  val password: String,
  val inviteCode: String?,
) {
  override fun toString(): String {
    return "Credentials(" +
        "email='$email', " +
        "username='$username', " +
        "password='███', " +
        "inviteCode='$inviteCode'" +
        ")"
  }
}
