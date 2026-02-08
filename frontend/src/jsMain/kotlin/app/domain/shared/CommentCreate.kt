package app.domain.shared

import kotlinx.serialization.Serializable

@Serializable
data class CommentCreate(val text: String)

