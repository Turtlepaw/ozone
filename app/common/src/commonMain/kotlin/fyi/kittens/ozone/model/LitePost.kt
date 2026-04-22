package fyi.kittens.ozone.model

import app.bsky.feed.Post
import kotlinx.collections.immutable.persistentListOf
import fyi.kittens.ozone.util.ReadOnlyList
import fyi.kittens.ozone.util.mapNotNullImmutable

data class LitePost(
  val text: String,
  val links: ReadOnlyList<TimelinePostLink>,
  val createdAt: Moment,
)

fun Post.toLitePost(): LitePost {
  return LitePost(
    text = text,
    links = facets?.mapNotNullImmutable { it.toLinkOrNull() } ?: persistentListOf(),
    createdAt = Moment(createdAt),
  )
}
