package com.anjunar.technologyspeaks.hibernate.search

import jakarta.persistence.EntityManager
import jakarta.persistence.criteria.Expression
import jakarta.persistence.criteria.Order
import jakarta.persistence.criteria.Predicate
import org.hibernate.Session
import org.hibernate.query.criteria.HibernateCriteriaBuilder
import org.hibernate.query.criteria.JpaCriteriaQuery
import org.hibernate.query.criteria.JpaRoot
import org.springframework.beans.factory.ObjectProvider
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
@Component
class HibernateSearch(
    val entityManager: EntityManager,
    val predicateProvider: ObjectProvider<out PredicateProvider<*,*>>,
    val sortProvider: ObjectProvider<out SortProvider<*,*>>
) {

    fun <S : AbstractSearch> searchContext(search: S): HibernateSearchContext {
        return object : HibernateSearchContext {
            override fun <C> apply(
                session: Session,
                builder: HibernateCriteriaBuilder,
                query: JpaCriteriaQuery<*>,
                root: JpaRoot<C>
            ): HibernateSearchContextResult {
                return SearchBeanReader.read(search, session, builder, root, query, predicateProvider as ObjectProvider<PredicateProvider<Any, C>>)
            }

            override fun <C> sort(
                session: Session,
                builder: HibernateCriteriaBuilder,
                query: JpaCriteriaQuery<*>,
                root: JpaRoot<C>,
                predicates: MutableList<Predicate>,
                selection: MutableList<Expression<*>>
            ): MutableList<Order> {
                return SearchBeanReader.order(search, session, builder, root, query, predicates, selection, sortProvider as ObjectProvider<SortProvider<Any, C>>)
            }
        }
    }

    fun <E : Any, P : Any> entities(
        index: Int,
        limit: Int,
        entityClass: KClass<E>,
        projection: KClass<P>,
        context: HibernateSearchContext,
        select: (JpaCriteriaQuery<P>, JpaRoot<E>, MutableList<Expression<*>>, HibernateCriteriaBuilder) -> JpaCriteriaQuery<P>
    ): List<P> {
        val session = entityManager.unwrap(Session::class.java)
        val builder = session.criteriaBuilder
        val query = builder.createQuery(projection.java)
        val from = query.from(entityClass.java)

        val (selection, predicates, parameters) = context.apply(session, builder, query, from)

        val order = context.sort(session, builder, query, from, predicates, selection)

        select(query, from, selection, builder).where(predicates).orderBy(order)

        val results = session.createQuery(query)
            .setFirstResult(index)
            .setMaxResults(limit)

        parameters.forEach { (key, value) -> results.setParameter(key, value) }

        return results.resultList
    }

    fun <E : Any> count(
        entityClass: KClass<E>,
        context: HibernateSearchContext
    ): Long {
        val session = entityManager.unwrap(Session::class.java)
        val builder = session.criteriaBuilder
        val query = builder.createQuery(Long::class.java)
        val from = query.from(entityClass.java)

        val (selection, predicates, parameters) = context.apply(session, builder, query, from)

        query.select(builder.count()).where(predicates)

        val results = session.createQuery(query)

        parameters.forEach { (key, value) -> results.setParameter(key, value) }

        return results.singleResult
    }


}