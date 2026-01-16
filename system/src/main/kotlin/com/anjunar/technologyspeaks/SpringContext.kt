package com.anjunar.technologyspeaks

import jakarta.persistence.EntityManager
import org.springframework.context.ApplicationContext
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.context.support.WebApplicationContextUtils
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
class SpringContext {

    companion object {

        fun entityManager() : EntityManager {
            return context.getBean(EntityManager::class.java)
        }

        fun <C : Any>getBean(clazz : KClass<C>) : C {
            return context.getBean(clazz.java)
        }

        lateinit var context: ApplicationContext

    }

}