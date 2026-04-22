package fyi.kittens.ozone.api.generator

import fyi.kittens.ozone.api.generator.builder.parseLexiconRef
import fyi.kittens.ozone.api.lexicon.LexiconDocument
import fyi.kittens.ozone.api.lexicon.LexiconSingleReference
import fyi.kittens.ozone.api.lexicon.LexiconUserType
import fyi.kittens.ozone.api.lexicon.parseDocument
import fyi.kittens.ozone.api.lexicon.parseDocumentMetadata
import java.io.File

class LexiconProcessingEnvironment(
  allLexiconSchemaJsons: List<String>,
  val defaults: DefaultsConfiguration,
  val outputDirectory: File,
) : Iterable<String> {
  private val schemasById: Map<String, String>
  private val schemaCache = mutableMapOf<String, LexiconDocument>()

  init {
    schemasById = allLexiconSchemaJsons.associateBy { parseDocumentMetadata(it).id }
  }

  fun loadDocument(schemaId: String): LexiconDocument {
    return schemaCache.getOrPut(schemaId) {
      parseDocument(schemasById[schemaId]!!)
    }
  }

  fun loadReferenceDocument(
    source: LexiconDocument,
    reference: LexiconSingleReference,
  ): LexiconDocument {
    val (lexiconId, _) = reference.ref.parseLexiconRef(source)

    return if (lexiconId.isEmpty()) {
      source
    } else {
      loadDocument(lexiconId)
    }
  }

  fun loadReference(
    source: LexiconDocument,
    reference: LexiconSingleReference,
  ): LexiconUserType {
    val (lexiconId, objectRef) = reference.ref.parseLexiconRef(source)

    val lexiconDocument = if (lexiconId.isEmpty()) {
      source
    } else {
      loadDocument(lexiconId)
    }

    return lexiconDocument.defs[objectRef]!!
  }

  override fun iterator(): Iterator<String> = schemasById.keys.iterator()
}
