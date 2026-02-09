package com.anjunar.technologyspeaks.core

import com.anjunar.technologyspeaks.hibernate.search.AbstractSearch
import jakarta.json.bind.annotation.JsonbProperty

class UserSearch : AbstractSearch() {

    @JsonbProperty lateinit var name : String

}