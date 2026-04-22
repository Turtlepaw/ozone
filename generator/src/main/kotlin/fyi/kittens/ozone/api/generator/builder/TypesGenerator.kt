package fyi.kittens.ozone.api.generator.builder

import fyi.kittens.ozone.api.lexicon.LexiconUserType

interface TypesGenerator {
  fun generateTypes(
    context: GeneratorContext,
    userType: LexiconUserType,
  )
}
