package fyi.kittens.ozone

import kotlinx.serialization.json.Json
import fyi.kittens.ozone.api.runtime.buildXrpcJsonConfiguration
import fyi.kittens.ozone.api.xrpc.XrpcSerializersModule

/**
 * JSON configuration for serializing and deserializing Bluesky API objects.
 */
val BlueskyJson: Json = buildXrpcJsonConfiguration(XrpcSerializersModule)
