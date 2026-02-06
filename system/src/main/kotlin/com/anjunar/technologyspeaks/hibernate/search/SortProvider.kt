package com.anjunar.technologyspeaks.hibernate.search

import jakarta.persistence.criteria.Order

interface SortProvider<V, E> {

    fun sort(context : Context<V, E>): MutableList<Order>

}