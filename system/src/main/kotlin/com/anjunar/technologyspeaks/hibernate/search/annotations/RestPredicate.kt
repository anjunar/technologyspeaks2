package com.anjunar.technologyspeaks.hibernate.search.annotations

import com.anjunar.technologyspeaks.hibernate.search.PredicateProvider
import kotlin.reflect.KClass

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class RestPredicate(val value : KClass<out PredicateProvider<*, *>>, val name : String = "")