package com.anjunar.kotlin.universe.introspector

import com.anjunar.kotlin.universe.ResolvedClass
import com.anjunar.kotlin.universe.TypeResolver
import java.lang.reflect.Type
import java.util.HashMap
import java.util.Objects

object BeanIntrospector {

    private val cache: MutableMap<ResolvedClass, BeanModel> = HashMap()

    fun create(aClass: ResolvedClass): BeanModel {
        var beanModel = cache[aClass]
        if (Objects.isNull(beanModel)) {
            beanModel = BeanModel(aClass)
            cache[aClass] = beanModel
        }
        return beanModel!!
    }

    fun createWithType(aType: Type): BeanModel =
        create(TypeResolver.resolve(aType))
}