package com.anjunar.technologyspeaks.core

import com.anjunar.technologyspeaks.hibernate.search.AbstractSearch

class UserSearch(val name : String?,
                 sort : MutableList<String> = mutableListOf(),
                 index : Int = 0,
                 limit : Int = 5) : AbstractSearch(sort,index,limit)