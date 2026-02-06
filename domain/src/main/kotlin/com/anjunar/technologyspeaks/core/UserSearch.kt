package com.anjunar.technologyspeaks.core

import com.anjunar.technologyspeaks.hibernate.search.AbstractSearch
import jakarta.json.bind.annotation.JsonbProperty

class UserSearch(@JsonbProperty val name : String?,
                 sort : MutableList<String> = mutableListOf(),
                 index : Int = 0,
                 limit : Int = 5) : AbstractSearch(sort,index,limit)