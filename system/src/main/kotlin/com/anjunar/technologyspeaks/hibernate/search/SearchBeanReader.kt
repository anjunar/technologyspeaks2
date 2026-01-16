package com.anjunar.technologyspeaks.hibernate.search

import com.anjunar.kotlin.universe.introspector.BeanIntrospector
import com.anjunar.technologyspeaks.hibernate.search.annotations.RestPredicate
import com.anjunar.technologyspeaks.hibernate.search.annotations.RestSort
import jakarta.persistence.criteria.Expression
import jakarta.persistence.criteria.Predicate
import org.hibernate.Session
import org.hibernate.query.Order
import org.hibernate.query.criteria.HibernateCriteriaBuilder
import org.hibernate.query.criteria.JpaCriteriaQuery
import org.hibernate.query.criteria.JpaRoot
import org.springframework.beans.factory.ObjectProvider

object SearchBeanReader {

    fun <E> read(
        searchBean: AbstractSearch,
        session: Session,
        builder: HibernateCriteriaBuilder,
        root: JpaRoot<E>,
        query: JpaCriteriaQuery<*>,
        instances: ObjectProvider<PredicateProvider<Any, E>>
    ): HibernateSearchContextResult {

        val beanModel = BeanIntrospector.createWithType(searchBean::class.java)

        val predicates = ArrayList<Predicate>()
        val selection = ArrayList<Expression<*>>()
        val parameters = HashMap<String, Any>()

        for (property in beanModel.properties) {

            val restPredicate = property.findAnnotation(RestPredicate::class.java)

            if (restPredicate != null) {

                val provider =
                    instances.find { predicateProvider -> predicateProvider.javaClass == restPredicate.value.java }
                val value = property.get(searchBean)

                if (provider != null && value != null) {

                    val name = restPredicate.name.ifBlank { property.name }

                    provider.build(
                        Context(
                            value,
                            session,
                            builder,
                            predicates,
                            root,
                            query,
                            selection,
                            name,
                            parameters
                        )
                    )

                }

            }

        }

        return HibernateSearchContextResult(selection, predicates, parameters)
    }

    fun <E> order(
        searchBean: AbstractSearch,
        session: Session,
        builder: HibernateCriteriaBuilder,
        root: JpaRoot<E>,
        query: JpaCriteriaQuery<*>,
        predicates: MutableList<Predicate>,
        selection: MutableList<Expression<*>>,
        instances: ObjectProvider<SortProvider<Any, E>>
    ): MutableList<Order<E>> {

        val beanModel = BeanIntrospector.createWithType(searchBean::class.java)

        for (property in beanModel.properties) {

            val restSort = property.findAnnotation(RestSort::class.java)

            if (restSort != null) {

                val value = property.get(searchBean)

                if (value != null) {

                    val sortProvider = instances.find { provider -> provider.javaClass == restSort.value.java }

                    if (sortProvider != null) {
                        return sortProvider.sort(
                            Context(
                                searchBean,
                                session,
                                builder,
                                predicates,
                                root,
                                query,
                                selection,
                                property.name,
                                HashMap()
                            )
                        )

                    }
                }
            }
        }

        return ArrayList()

    }

}