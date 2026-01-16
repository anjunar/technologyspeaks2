package com.anjunar.technologyspeaks.hibernate.search

interface PredicateProvider<V,E> {

    fun build(context : Context<V,E>)

}