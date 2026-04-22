package fyi.kittens.ozone.thread

import fyi.kittens.ozone.api.AtUri
import fyi.kittens.ozone.api.Cid
import fyi.kittens.ozone.model.Reference
import fyi.kittens.ozone.model.TimelinePost

sealed interface ThreadProps {
  val uri: AtUri
  val cid: Cid
  val originalPost: TimelinePost?

  data class FromPost(
    override val originalPost: TimelinePost,
  ) : ThreadProps {
    override val uri: AtUri = originalPost.uri
    override val cid: Cid = originalPost.cid
  }

  data class FromReference(
    val reference: Reference,
  ) : ThreadProps {
    override val originalPost: TimelinePost? = null
    override val uri: AtUri = reference.uri
    override val cid: Cid = reference.cid
  }
}
