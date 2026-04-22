package fyi.kittens.ozone.api.generator

import com.squareup.kotlinpoet.MemberName
import kotlin.properties.ReadOnlyProperty

val defaultHttpClient by memberOfPackage("fyi.kittens.ozone.api.xrpc")

val findSubscriptionSerializer by memberOfPackage("fyi.kittens.ozone.api.xrpc")

val polymorphic by extensionMemberOfPackage("kotlinx.serialization.modules")

val procedure by extensionMemberOfPackage("fyi.kittens.ozone.api.xrpc")

val query by extensionMemberOfPackage("fyi.kittens.ozone.api.xrpc")

val runBlocking by memberOfPackage("kotlinx.coroutines")

val stringEnumSerializer by memberOfPackage("fyi.kittens.ozone.api.runtime")

val subscription by extensionMemberOfPackage("fyi.kittens.ozone.api.xrpc")

val toAtpResponse by extensionMemberOfPackage("fyi.kittens.ozone.api.xrpc")

val toAtpResult by extensionMemberOfPackage("fyi.kittens.ozone.api.xrpc")

val toAtpModel by extensionMemberOfPackage("fyi.kittens.ozone.api.xrpc")

val withXrpcConfiguration by extensionMemberOfPackage("fyi.kittens.ozone.api.xrpc")

private fun memberOfPackage(packageName: String): ReadOnlyProperty<Any?, MemberName> {
  return ReadOnlyProperty { _, property -> MemberName(packageName, property.name) }
}

private fun extensionMemberOfPackage(packageName: String): ReadOnlyProperty<Any?, MemberName> {
  return ReadOnlyProperty { _, property -> MemberName(packageName, property.name, isExtension = true) }
}
