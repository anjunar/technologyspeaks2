package app.domain.shared

import app.domain.core.AbstractEntity
import app.domain.core.Link
import app.domain.core.User
import jFx2.forms.editor.EditorNode
import jFx2.forms.editor.NodeSerializer
import jFx2.state.ListProperty
import jFx2.state.ListPropertySerializer
import jFx2.state.Property
import jFx2.state.PropertySerializer
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonElement
import kotlin.time.Clock

@Serializable
class SecondComment(
    @Serializable(with = PropertySerializer::class)
    override var id: Property<String>? = null,
    @Serializable(with = PropertySerializer::class)
    override val modified : Property<LocalDateTime> = Property(Clock.System.now().toLocalDateTime(TimeZone.UTC)),
    @Serializable(with = PropertySerializer::class)
    override val created : Property<LocalDateTime> = Property(Clock.System.now().toLocalDateTime(TimeZone.UTC)),
    @Serializable(with = PropertySerializer::class)
    override var user: Property<User>? = null,
    @Serializable(with = NodeSerializer::class)
    val editor: Property<EditorNode?> = Property(null),
    @Serializable(with = ListPropertySerializer::class)
    val likes : ListProperty<Like> = ListProperty(),
    @SerialName($$"$links")
    @Serializable(with = ListPropertySerializer::class)
    override val links : ListProperty<Link> = ListProperty()
) : AbstractEntity, OwnerProvider {

    @Transient
    val editable = Property(false)

}

