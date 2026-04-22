package fyi.kittens.ozone.compose

import fyi.kittens.ozone.model.Profile
import fyi.kittens.ozone.model.Reference
import fyi.kittens.ozone.model.TimelinePost

data class ComposePostProps(
  val replyTo: PostReplyInfo? = null,
)

data class PostReplyInfo(
  val parent: Reference,
  val root: Reference,
  val parentAuthor: Profile,
)

fun TimelinePost.asReplyInfo(): PostReplyInfo {
  return PostReplyInfo(
    parent = Reference(uri, cid),
    root = (reply?.root ?: this).let { Reference(it.uri, it.cid) },
    parentAuthor = author,
  )
}
