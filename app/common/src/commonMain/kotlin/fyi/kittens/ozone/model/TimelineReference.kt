package fyi.kittens.ozone.model

import com.atproto.repo.StrongRef
import fyi.kittens.ozone.api.AtUri
import fyi.kittens.ozone.api.Cid

data class TimelineReference(
  val uri: AtUri,
  val cid: Cid,
)

fun StrongRef.toReference(): TimelineReference {
  return TimelineReference(
    uri = uri,
    cid = cid,
  )
}
