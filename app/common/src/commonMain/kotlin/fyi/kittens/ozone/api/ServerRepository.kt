package fyi.kittens.ozone.api

import kotlinx.coroutines.flow.Flow
import me.tatarka.inject.annotations.Inject
import fyi.kittens.ozone.di.SingleInApp
import fyi.kittens.ozone.login.auth.Server
import fyi.kittens.ozone.store.PersistentStorage
import fyi.kittens.ozone.store.preference

@Inject
@SingleInApp
class ServerRepository(
  storage: PersistentStorage,
) {
  private val serverPreference = storage.preference<Server>("servers", Server.BlueskySocial)

  var server: Server by serverPreference

  fun serverFlow(): Flow<Server> = serverPreference.updates
}
