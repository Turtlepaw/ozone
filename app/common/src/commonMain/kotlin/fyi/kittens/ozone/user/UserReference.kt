package fyi.kittens.ozone.user

import fyi.kittens.ozone.api.Did
import fyi.kittens.ozone.api.Handle
import kotlin.jvm.JvmInline

sealed interface UserReference

@JvmInline
value class UserDid(
  val did: Did,
) : UserReference {
  override fun toString(): String = did.did
}

@JvmInline
value class UserHandle(
  val handle: Handle,
) : UserReference {
  override fun toString(): String = handle.handle
}
