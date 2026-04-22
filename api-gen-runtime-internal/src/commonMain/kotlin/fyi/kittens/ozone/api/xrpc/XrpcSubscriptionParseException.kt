package fyi.kittens.ozone.api.xrpc

import fyi.kittens.ozone.api.response.AtpErrorDescription

class XrpcSubscriptionParseException(
  val error: AtpErrorDescription?,
) : RuntimeException("Subscription result could not be parsed")
