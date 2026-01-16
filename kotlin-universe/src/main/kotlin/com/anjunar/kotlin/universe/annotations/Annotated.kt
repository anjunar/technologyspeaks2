package com.anjunar.kotlin.universe.annotations

interface Annotated {

    val declaredAnnotations: Array<Annotation>

    val annotations: Array<Annotation>

    fun <A : Annotation> findAnnotation(aClass: Class<A>): A? = annotations.firstOrNull { anno -> anno.annotationClass == aClass.kotlin } as A?

    fun <A : Annotation> findDeclaredAnnotation(aClass: Class<A>): A? = declaredAnnotations.firstOrNull { anno -> anno.annotationClass == aClass.kotlin } as A?

}