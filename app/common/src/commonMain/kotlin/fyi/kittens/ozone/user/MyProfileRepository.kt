package fyi.kittens.ozone.user

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import me.tatarka.inject.annotations.Inject
import fyi.kittens.ozone.api.ApiProvider
import fyi.kittens.ozone.app.Supervisor
import fyi.kittens.ozone.di.SingleInApp
import fyi.kittens.ozone.login.LoginRepository
import fyi.kittens.ozone.model.FullProfile

@Inject
@SingleInApp
class MyProfileRepository(
  private val apiProvider: ApiProvider,
  private val userDatabase: UserDatabase,
  private val loginRepository: LoginRepository,
) : Supervisor() {
  private val profileFlow = MutableStateFlow<FullProfile?>(null)

  @OptIn(ExperimentalCoroutinesApi::class)
  override suspend fun CoroutineScope.onStart() {
    loginRepository.authFlow().flatMapLatest { auth ->
      auth?.did
        ?.let { did -> userDatabase.profile(UserDid(did)) }
        ?: flowOf(null)
    }.collect(profileFlow)
  }

  fun me(): StateFlow<FullProfile?> = profileFlow

  fun isMe(userReference: UserReference): Boolean {
    return when (userReference) {
      is UserDid -> userReference.did == profileFlow.value!!.did
      is UserHandle -> userReference.handle == profileFlow.value!!.handle
    }
  }
}
