package com.anjunar.technologyspeaks.hibernate

import com.anjunar.technologyspeaks.SpringContext
import jakarta.persistence.EntityManager

@Suppress("UNCHECKED_CAST")
interface EntityContext<E> {

    fun entityManager() : EntityManager = SpringContext.entityManager()

    fun persist() {
        entityManager().persist(this)
    }

    fun merge() : E {
        return entityManager().merge(this as E)
    }

    fun remove() {
        entityManager().remove(this)
    }

}