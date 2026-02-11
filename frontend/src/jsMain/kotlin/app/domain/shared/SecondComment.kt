package app.domain.shared

import app.domain.core.AbstractEntity
import app.domain.core.User
import jFx2.forms.editor.EditorNode
import jFx2.forms.editor.NodeSerializer
import jFx2.state.Property
import jFx2.state.PropertySerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonElement

@Serializable
class SecondComment(
    @Serializable(with = PropertySerializer::class)
    override var id: Property<String>? = null,
    @Serializable(with = PropertySerializer::class)
    override val user: Property<User>? = null,
    @Serializable(with = NodeSerializer::class)
    val editor: Property<EditorNode?> = Property(null)
) : AbstractEntity, OwnerProvider {

    @Transient
    val editable = Property(false)

}

