package app.domain.core

import kotlinx.serialization.Serializable

@Serializable
class Table<E>(val rows : List<Data<E>> = emptyList(), val size : Int)