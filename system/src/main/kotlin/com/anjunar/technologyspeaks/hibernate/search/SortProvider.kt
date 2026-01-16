package com.anjunar.technologyspeaks.hibernate.search

import org.hibernate.query.Order

interface SortProvider<V, E> {

    fun sort(context : Context<V, E>): MutableList<Order<E>>

}