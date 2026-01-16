package com.anjunar.technologyspeaks.hibernate

import com.anjunar.technologyspeaks.SpringContext
import jakarta.persistence.Entity
import jakarta.persistence.EntityGraph
import jakarta.persistence.EntityManager
import jakarta.persistence.NoResultException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.reflect.ParameterizedType

abstract class RepositoryContext<E : Any> {

    val log: Logger = LoggerFactory.getLogger(RepositoryContext::class.java)

    @Suppress("UNCHECKED_CAST")
    val clazz: Class<E> by lazy {
        val superClass = javaClass.genericSuperclass
        require(superClass is ParameterizedType) {
            "RepositoryContext must be subclassed with generic type"
        }
        superClass.actualTypeArguments[0] as Class<E>
    }

    fun entityManager(): EntityManager = SpringContext.entityManager()

    fun find(id: Any): E? {
        return entityManager().find(clazz, id)
    }

    fun find(graph: EntityGraph<E>, id: Any): E? {
        val em = entityManager()
        val hints = mapOf<String, Any>("jakarta.persistence.loadgraph" to graph)
        return em.find(clazz, id, hints)
    }

    fun findAll(): List<E> {
        val em = entityManager()
        val cb = em.criteriaBuilder
        val cq = cb.createQuery(clazz)
        val root = cq.from(clazz)
        cq.select(root)
        return em.createQuery(cq).resultList
    }

    fun query(vararg parameters: Pair<String, Any>): E? {
        val em = entityManager()

        val entityAnnotation: Entity? = clazz.getAnnotation(Entity::class.java)
        var entityName: String = entityAnnotation?.name ?: ""
        if (entityName.isEmpty()) {
            entityName = clazz.simpleName
        }

        val sqlParams = parameters.joinToString(" and ") { "e.${it.first} = :${it.first}" }
        val jpql = "select e from $entityName e where $sqlParams"
        val typedQuery = em.createQuery(jpql, clazz)

        for ((key, value) in parameters) {
            typedQuery.setParameter(key, value)
        }

        return try {
            typedQuery.singleResult
        } catch (ex: NoResultException) {
            null
        }
    }
}