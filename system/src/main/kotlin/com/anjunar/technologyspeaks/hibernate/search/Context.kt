package com.anjunar.technologyspeaks.hibernate.search

import jakarta.persistence.criteria.Expression
import jakarta.persistence.criteria.Predicate
import org.hibernate.Session
import org.hibernate.query.criteria.HibernateCriteriaBuilder
import org.hibernate.query.criteria.JpaCriteriaQuery
import org.hibernate.query.criteria.JpaRoot

data class Context<V,E>(
    val value: V,
    val session: Session,
    val builder: HibernateCriteriaBuilder,
    val predicates : MutableList<Predicate>,
    val root: JpaRoot<E>,
    val query: JpaCriteriaQuery<*>,
    val selection : MutableList<Expression<*>>,
    val name: String,
    val parameters: MutableMap<String, Any>
)
