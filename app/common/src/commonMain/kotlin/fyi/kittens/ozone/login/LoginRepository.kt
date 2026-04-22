package fyi.kittens.ozone.login

import kotlinx.coroutines.flow.Flow
import me.tatarka.inject.annotations.Inject
import fyi.kittens.ozone.di.SingleInApp
import fyi.kittens.ozone.login.auth.AuthInfo
import fyi.kittens.ozone.store.PersistentStorage
import fyi.kittens.ozone.store.nullablePreference

@Inject
@SingleInApp
class LoginRepository(
  storage: PersistentStorage,
) {
  private val authPreference = storage.nullablePreference<AuthInfo>("auth-info", null)

  var auth: AuthInfo? by authPreference

  fun authFlow(): Flow<AuthInfo?> = authPreference.updates
}
