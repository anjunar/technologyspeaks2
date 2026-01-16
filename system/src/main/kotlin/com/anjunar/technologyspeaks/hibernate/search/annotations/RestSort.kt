package com.anjunar.technologyspeaks.hibernate.search.annotations

import com.anjunar.technologyspeaks.hibernate.search.SortProvider
import kotlin.reflect.KClass

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class RestSort(val value : KClass<out SortProvider<*,*>>)
