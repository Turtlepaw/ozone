package fyi.kittens.ozone.util

import io.ktor.http.URLParserException
import io.ktor.http.Url

fun String.isUrl(): Boolean {
  return try {
    Url(this).toString()
    true
  } catch (e: URLParserException) {
    false
  }
}
