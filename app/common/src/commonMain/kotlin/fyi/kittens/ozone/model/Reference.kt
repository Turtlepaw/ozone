package fyi.kittens.ozone.model

import fyi.kittens.ozone.api.AtUri
import fyi.kittens.ozone.api.Cid

data class Reference(
  val uri: AtUri,
  val cid: Cid,
)
