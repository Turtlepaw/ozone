@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package fyi.kittens.ozone.api.runtime

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import fyi.kittens.ozone.api.model.JsonContent

expect object JsonContentSerializer : KSerializer<JsonContent> {
  override val descriptor: SerialDescriptor
  override fun deserialize(decoder: Decoder): JsonContent
  override fun serialize(encoder: Encoder, value: JsonContent)
}
