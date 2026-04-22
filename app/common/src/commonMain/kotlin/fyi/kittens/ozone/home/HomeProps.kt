package fyi.kittens.ozone.home

import fyi.kittens.ozone.login.auth.AuthInfo

data class HomeProps(
  val authInfo: AuthInfo,
  val unreadNotificationCount: Int,
)
