package fyi.kittens.ozone.api.model

import kotlin.time.Instant
import kotlinx.serialization.Serializable
import fyi.kittens.ozone.api.runtime.LenientInstantIso8601Serializer

/**
 * A specific moment in time.
 */
typealias Timestamp = @Serializable(LenientInstantIso8601Serializer::class) Instant