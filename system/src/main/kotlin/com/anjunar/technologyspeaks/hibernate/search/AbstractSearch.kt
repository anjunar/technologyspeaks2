package com.anjunar.technologyspeaks.hibernate.search

abstract class AbstractSearch(val sort : MutableList<String> = mutableListOf(),
                              val index : Int = 0,
                              val limit : Int = 5)