package fyi.kittens.ozone.profile

import fyi.kittens.ozone.model.FullProfile
import fyi.kittens.ozone.user.UserReference

data class ProfileProps(
  val user: UserReference,
  val preloadedProfile: FullProfile? = null,
)
