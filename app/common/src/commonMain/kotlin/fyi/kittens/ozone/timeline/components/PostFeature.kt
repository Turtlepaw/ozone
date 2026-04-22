package fyi.kittens.ozone.timeline.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import fyi.kittens.ozone.model.EmbedPost
import fyi.kittens.ozone.model.Moment
import fyi.kittens.ozone.model.TimelinePostFeature
import fyi.kittens.ozone.model.TimelinePostFeature.ExternalFeature
import fyi.kittens.ozone.model.TimelinePostFeature.ImagesFeature
import fyi.kittens.ozone.model.TimelinePostFeature.MediaPostFeature
import fyi.kittens.ozone.model.TimelinePostFeature.PostFeature
import fyi.kittens.ozone.thread.ThreadProps
import fyi.kittens.ozone.timeline.components.feature.BlockedPostPost
import fyi.kittens.ozone.timeline.components.feature.InvisiblePostPost
import fyi.kittens.ozone.timeline.components.feature.PostExternal
import fyi.kittens.ozone.timeline.components.feature.PostImages
import fyi.kittens.ozone.timeline.components.feature.UnknownPostPost
import fyi.kittens.ozone.timeline.components.feature.VisiblePostPost
import fyi.kittens.ozone.ui.compose.OpenImageAction

@Composable
internal fun PostFeature(
  now: Moment,
  feature: TimelinePostFeature?,
  onOpenImage: (OpenImageAction) -> Unit,
  onOpenPost: (ThreadProps) -> Unit,
) {
  val uriHandler = LocalUriHandler.current
  when (feature) {
    is ImagesFeature -> PostImages(feature, onOpenImage)
    is ExternalFeature -> PostExternal(feature, onClick = {
      uriHandler.openUri(feature.uri.uri)
    })
    is PostFeature -> when (val embedPost = feature.post) {
      is EmbedPost.VisibleEmbedPost -> {
        VisiblePostPost(now, embedPost.litePost, embedPost.author, onClick = {
          onOpenPost(ThreadProps.FromReference(embedPost.reference))
        })
      }
      is EmbedPost.InvisibleEmbedPost -> InvisiblePostPost(onClick = {})
      is EmbedPost.BlockedEmbedPost -> BlockedPostPost(onClick = {})
      is EmbedPost.UnknownEmbedPost -> UnknownPostPost(onClick = {})
    }
    is MediaPostFeature -> {
      when (val embedMedia = feature.media) {
        is ImagesFeature -> PostImages(embedMedia, onOpenImage)
        is ExternalFeature -> PostExternal(embedMedia, onClick = {
          uriHandler.openUri(embedMedia.uri.uri)
        })
      }
      when (val embedPost = feature.post) {
        is EmbedPost.VisibleEmbedPost -> {
          VisiblePostPost(now, embedPost.litePost, embedPost.author, onClick = {
            onOpenPost(ThreadProps.FromReference(embedPost.reference))
          })
        }
        is EmbedPost.InvisibleEmbedPost -> InvisiblePostPost(onClick = {})
        is EmbedPost.BlockedEmbedPost -> BlockedPostPost(onClick = {})
        is EmbedPost.UnknownEmbedPost -> UnknownPostPost(onClick = {})
      }
    }
    null -> Unit
  }
}
