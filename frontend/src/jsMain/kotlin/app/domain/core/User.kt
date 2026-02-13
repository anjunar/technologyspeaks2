package app.domain.core

import jFx2.state.ListProperty
import jFx2.state.ListPropertySerializer
import jFx2.state.Property
import jFx2.state.PropertySerializer
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Clock

@Serializable
class User(
    @Serializable(with = PropertySerializer::class)
    override var id : Property<String>? = null,
    @Serializable(with = PropertySerializer::class)
    override val modified : Property<LocalDateTime> = Property(Clock.System.now().toLocalDateTime(TimeZone.UTC)),
    @Serializable(with = PropertySerializer::class)
    override val created : Property<LocalDateTime> = Property(Clock.System.now().toLocalDateTime(TimeZone.UTC)),
    @Serializable(with = PropertySerializer::class)
    val nickName: Property<String> = Property(""),
    @Serializable(with = PropertySerializer::class)
    val image : Property<Media?> = Property(Media()),
    var info: UserInfo? = null,
    var address: Address? = null,
    @Serializable(with = ListPropertySerializer::class)
    val emails: ListProperty<Email> = ListProperty(),
    @SerialName($$"$links")
    @Serializable(with = ListPropertySerializer::class)
    override val links : ListProperty<Link> = ListProperty()
)  : AbstractEntity {

    override fun toString(): String {
        return "User(id=$id, nickName=$nickName, image=$image, info=$info, address=$address, emails=$emails)"
    }
}
