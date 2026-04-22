package fyi.kittens.ozone.login.auth

import fyi.kittens.ozone.util.ReadOnlyList

data class ServerInfo(
  val inviteCodeRequired: Boolean,
  val availableUserDomains: ReadOnlyList<String>,
  val privacyPolicy: String?,
  val termsOfService: String?,
)
