package fyi.kittens.ozone.api.runtime

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import fyi.kittens.ozone.api.model.Blob

object BlobSerializer : JsonContentPolymorphicSerializer<Blob>(Blob::class) {
  override fun selectDeserializer(element: JsonElement): DeserializationStrategy<Blob> {
    return if (element.jsonObject.containsKey("ref")) {
      Blob.StandardBlob.serializer()
    } else {
      Blob.LegacyBlob.serializer()
    }
  }
}