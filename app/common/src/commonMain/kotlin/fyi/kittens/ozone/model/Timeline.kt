package fyi.kittens.ozone.model

import app.bsky.feed.FeedViewPost
import fyi.kittens.ozone.util.ReadOnlyList
import fyi.kittens.ozone.util.mapImmutable

data class Timeline(
  val posts: ReadOnlyList<TimelinePost>,
  val cursor: String?,
) {
  companion object {
    fun from(
      posts: List<FeedViewPost>,
      cursor: String?,
    ): Timeline {
      return Timeline(
        posts = posts.mapImmutable { it.toPost() },
        cursor = cursor,
      )
    }
  }
}
