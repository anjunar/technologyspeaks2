package com.anjunar.technologyspeaks.hibernate.search

import jakarta.persistence.criteria.Expression
import jakarta.persistence.criteria.Predicate

data class HibernateSearchContextResult(
    val selection : MutableList<Expression<*>>,
    val predicates : MutableList<Predicate>,
    val parameters : MutableMap<String, Any>
)
