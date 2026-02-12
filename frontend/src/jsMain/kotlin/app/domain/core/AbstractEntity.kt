package app.domain.core

import jFx2.state.ListProperty
import jFx2.state.Property

interface AbstractEntity {

    var id: Property<String>?

    val links: ListProperty<Link>


}