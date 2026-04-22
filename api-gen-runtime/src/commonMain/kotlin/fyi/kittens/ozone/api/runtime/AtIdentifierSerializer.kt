package fyi.kittens.ozone.api.runtime

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive
import fyi.kittens.ozone.api.AtIdentifier
import fyi.kittens.ozone.api.Did
import fyi.kittens.ozone.api.Handle

object AtIdentifierSerializer : JsonContentPolymorphicSerializer<AtIdentifier>(AtIdentifier::class) {
  override fun selectDeserializer(element: JsonElement): DeserializationStrategy<AtIdentifier> {
    return if (element.jsonPrimitive.content.startsWith("did:")) {
      Did.serializer()
    } else {
      Handle.serializer()
    }
  }
}
