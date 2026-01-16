package com.anjunar.technologyspeaks.hibernate.search

import jakarta.persistence.criteria.Expression
import jakarta.persistence.criteria.Predicate
import org.hibernate.Session
import org.hibernate.query.Order
import org.hibernate.query.criteria.HibernateCriteriaBuilder
import org.hibernate.query.criteria.JpaCriteriaQuery
import org.hibernate.query.criteria.JpaRoot


interface HibernateSearchContext {

    fun <C> apply(
        session: Session,
        builder: HibernateCriteriaBuilder,
        query: JpaCriteriaQuery<*>,
        root: JpaRoot<C>
    ): HibernateSearchContextResult

    fun <C> sort(
        session: Session,
        builder: HibernateCriteriaBuilder,
        query: JpaCriteriaQuery<*>,
        root: JpaRoot<C>,
        predicates: MutableList<Predicate>,
        selection: MutableList<Expression<*>>
    ): MutableList<Order<C>>

}