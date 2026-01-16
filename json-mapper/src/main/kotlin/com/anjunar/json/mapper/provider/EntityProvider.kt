package com.anjunar.json.mapper.provider

import java.util.UUID

interface EntityProvider {

    val id : UUID

    val version : Long

}