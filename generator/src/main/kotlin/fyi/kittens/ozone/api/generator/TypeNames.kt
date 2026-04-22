package fyi.kittens.ozone.api.generator

import com.squareup.kotlinpoet.ClassName
import kotlin.properties.ReadOnlyProperty

object TypeNames {
  val AtIdentifier by classOfPackage("fyi.kittens.ozone.api")
  val AtUri by classOfPackage("fyi.kittens.ozone.api")
  val AtpEnum by classOfPackage("fyi.kittens.ozone.api.model")
  val AtpResponse by classOfPackage("fyi.kittens.ozone.api.response")
  val Blob by classOfPackage("fyi.kittens.ozone.api.model")
  val Cid by classOfPackage("fyi.kittens.ozone.api")
  val Deprecated by classOfPackage("kotlin")
  val Did by classOfPackage("fyi.kittens.ozone.api")
  val Flow by classOfPackage("kotlinx.coroutines.flow")
  val Handle by classOfPackage("fyi.kittens.ozone.api")
  val HttpClient by classOfPackage("io.ktor.client")
  val JsonContent by classOfPackage("fyi.kittens.ozone.api.model")
  val KClass by classOfPackage("kotlin.reflect")
  val KSerializer by classOfPackage("kotlinx.serialization")
  val Language by classOfPackage("fyi.kittens.ozone.api")
  val Nsid by classOfPackage("fyi.kittens.ozone.api")
  val PermissionResource by classOfPackage("fyi.kittens.ozone.api.model.permissions")
  val PermissionSet by classOfPackage("fyi.kittens.ozone.api.model.permissions")
  val RepoPermission by classOfPackage("fyi.kittens.ozone.api.model.permissions")
  val Result by classOfPackage("kotlin")
  val RpcPermission by classOfPackage("fyi.kittens.ozone.api.model.permissions")
  val RKey by classOfPackage("fyi.kittens.ozone.api")
  val SerialName by classOfPackage("kotlinx.serialization")
  val Serializable by classOfPackage("kotlinx.serialization")
  val SerializersModule by classOfPackage("kotlinx.serialization.modules")
  val Suppress by classOfPackage("kotlin")
  val Tid by classOfPackage("fyi.kittens.ozone.api")
  val Timestamp by classOfPackage("fyi.kittens.ozone.api.model")
  val Uri by classOfPackage("fyi.kittens.ozone.api")
}

private fun classOfPackage(packageName: String): ReadOnlyProperty<Any?, ClassName> {
  return ReadOnlyProperty { _, property -> ClassName(packageName, property.name) }
}
