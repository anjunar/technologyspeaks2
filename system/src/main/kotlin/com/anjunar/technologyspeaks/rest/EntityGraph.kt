package com.anjunar.technologyspeaks.rest

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class EntityGraph(val value : String)
