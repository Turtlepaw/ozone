package fyi.kittens.ozone.api.generator.builder

import fyi.kittens.ozone.api.lexicon.LexiconArray
import fyi.kittens.ozone.api.lexicon.LexiconArrayItem
import fyi.kittens.ozone.api.lexicon.LexiconBlob
import fyi.kittens.ozone.api.lexicon.LexiconBoolean
import fyi.kittens.ozone.api.lexicon.LexiconBytes
import fyi.kittens.ozone.api.lexicon.LexiconCidLink
import fyi.kittens.ozone.api.lexicon.LexiconFloat
import fyi.kittens.ozone.api.lexicon.LexiconInteger
import fyi.kittens.ozone.api.lexicon.LexiconIpldType
import fyi.kittens.ozone.api.lexicon.LexiconObjectProperty
import fyi.kittens.ozone.api.lexicon.LexiconPrimitive
import fyi.kittens.ozone.api.lexicon.LexiconPrimitiveArray
import fyi.kittens.ozone.api.lexicon.LexiconReference
import fyi.kittens.ozone.api.lexicon.LexiconString
import fyi.kittens.ozone.api.lexicon.LexiconUnknown

fun LexiconObjectProperty.requirements(): List<Requirement> = when (this) {
  is LexiconObjectProperty.Array -> array.requirements()
  is LexiconObjectProperty.Blob -> blob.requirements()
  is LexiconObjectProperty.IpldType -> ipld.requirements()
  is LexiconObjectProperty.Primitive -> primitive.requirements()
  is LexiconObjectProperty.Reference -> reference.requirements()
}

fun LexiconArrayItem.requirements(): List<Requirement> = when (this) {
  is LexiconArrayItem.Blob -> blob.requirements()
  is LexiconArrayItem.IpldType -> ipld.requirements()
  is LexiconArrayItem.Primitive -> primitive.requirements()
  is LexiconArrayItem.Reference -> reference.requirements()
}

fun LexiconArray.requirements(): List<Requirement> = listOfNotNull(
  minLength?.let(Requirement::MinLength),
  maxLength?.let(Requirement::MaxLength),
) + items.requirements()

fun LexiconPrimitiveArray.requirements(): List<Requirement> = listOfNotNull(
  minLength?.let(Requirement::MinLength),
  maxLength?.let(Requirement::MaxLength),
) + items.requirements()

fun LexiconReference.requirements(): List<Requirement> = emptyList()

fun LexiconBlob.requirements(): List<Requirement> = emptyList()
// TODO enforce maxSize value when blobs are parsed to a proper data model, not a JsonElement.
// listOfNotNull(
//  maxSize?.toLong()?.let(Requirement::MaxLength),
//)

fun LexiconIpldType.requirements(): List<Requirement> = when (this) {
  is LexiconBytes -> {
    listOfNotNull(
      minLength?.toLong()?.let(Requirement::MinLength),
      maxLength?.toLong()?.let(Requirement::MaxLength),
    )
  }
  is LexiconCidLink -> emptyList()
}

fun LexiconPrimitive.requirements(): List<Requirement> = when (this) {
  is LexiconBoolean -> emptyList()
  is LexiconFloat -> {
    listOfNotNull(
      minimum?.let(Requirement::MinValue),
      maximum?.let(Requirement::MaxValue),
    )
  }
  is LexiconInteger -> {
    listOfNotNull(
      minimum?.let(Requirement::MinValue),
      maximum?.let(Requirement::MaxValue),
    )
  }
  is LexiconString -> {
    if (knownValues.isNotEmpty()) {
      listOfNotNull(
        minLength?.let(Requirement::MinToStringLength),
        maxLength?.let(Requirement::MaxToStringLength),
      )
    } else {
      listOfNotNull(
        minLength?.let(Requirement::MinLength),
        maxLength?.let(Requirement::MaxLength),
      )
    }
  }
  is LexiconUnknown -> emptyList()
}
