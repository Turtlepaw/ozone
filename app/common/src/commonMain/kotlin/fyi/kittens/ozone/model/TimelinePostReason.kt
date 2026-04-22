package fyi.kittens.ozone.model

import app.bsky.feed.FeedViewPostReasonUnion
import fyi.kittens.ozone.model.TimelinePostReason.TimelinePostPin
import fyi.kittens.ozone.model.TimelinePostReason.TimelinePostRepost

sealed interface TimelinePostReason {
  data class TimelinePostRepost(
    val repostAuthor: Profile,
    val indexedAt: Moment,
  ) : TimelinePostReason

  data object TimelinePostPin : TimelinePostReason
}

fun FeedViewPostReasonUnion.toReasonOrNull(): TimelinePostReason? {
  return when (this) {
    is FeedViewPostReasonUnion.ReasonRepost -> {
      TimelinePostRepost(
        repostAuthor = value.by.toProfile(),
        indexedAt = Moment(value.indexedAt),
      )
    }
    is FeedViewPostReasonUnion.ReasonPin -> {
      TimelinePostPin
    }
    is FeedViewPostReasonUnion.Unknown -> {
      null
    }
  }
}
