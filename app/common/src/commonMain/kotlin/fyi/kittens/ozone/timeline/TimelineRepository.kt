package fyi.kittens.ozone.timeline

import app.bsky.feed.GetTimelineQueryParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import me.tatarka.inject.annotations.Inject
import fyi.kittens.ozone.api.ApiProvider
import fyi.kittens.ozone.api.response.AtpResponse
import fyi.kittens.ozone.app.Supervisor
import fyi.kittens.ozone.di.SingleInApp
import fyi.kittens.ozone.error.ErrorProps
import fyi.kittens.ozone.error.toErrorProps
import fyi.kittens.ozone.login.LoginRepository
import fyi.kittens.ozone.model.Timeline
import fyi.kittens.ozone.util.toReadOnlyList

@Inject
@SingleInApp
class TimelineRepository(
  private val apiProvider: ApiProvider,
  private val loginRepository: LoginRepository,
): Supervisor() {
  private val latest: MutableStateFlow<Timeline?> = MutableStateFlow(null)
  private val loadErrors: MutableSharedFlow<ErrorProps> = MutableSharedFlow()

  val timeline: Flow<Timeline> = latest.filterNotNull()
  val errors: Flow<ErrorProps> = loadErrors

  override suspend fun CoroutineScope.onStart() {
    loginRepository.authFlow().filter { it == null }.collect {
      latest.value = null
    }
  }

  suspend fun refresh() {
    load(null)
  }

  suspend fun loadMore() {
    load(latest.value?.cursor)
  }

  private suspend fun load(cursor: String?) {
    val response: AtpResponse<Timeline> = apiProvider.api
      .getTimeline(GetTimelineQueryParams(limit = 100, cursor = cursor))
      .map { Timeline.from(it.feed, it.cursor) }

    when (response) {
      is AtpResponse.Success -> {
        val previousTimeline = latest.value
        val nextTimeline = response.response
        val mergedTimeline = if (cursor != null && previousTimeline != null) {
          Timeline(
            posts = (previousTimeline.posts + nextTimeline.posts).toReadOnlyList(),
            cursor = nextTimeline.cursor,
          )
        } else {
          nextTimeline
        }

        latest.value = mergedTimeline
      }
      is AtpResponse.Failure -> {
        loadErrors.emit(
          response.toErrorProps(true)
            ?: ErrorProps("Oops.", "Could not load timeline", true)
        )
      }
    }
  }
}
