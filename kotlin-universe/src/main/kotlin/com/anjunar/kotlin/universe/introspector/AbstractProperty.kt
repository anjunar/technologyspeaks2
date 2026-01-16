package com.anjunar.kotlin.universe.introspector

import com.anjunar.kotlin.universe.ResolvedClass
import com.anjunar.kotlin.universe.TypeResolver
import com.anjunar.kotlin.universe.annotations.Annotated
import com.anjunar.kotlin.universe.members.ResolvedField
import com.anjunar.kotlin.universe.members.ResolvedMethod
import com.google.common.reflect.TypeToken

abstract class AbstractProperty(val name: String, val javaField: ResolvedField?, val getter: ResolvedMethod?, val setter: ResolvedMethod?) : Annotated {

    fun get(instance: Any): Any? {
        return if (getter == null) {
            javaField!!.get(instance)
        } else {
            getter.invoke(instance)
        }
    }

    fun set(instance: Any, value: Any?) {
        if (setter == null) {
            throw IllegalStateException("no Setter Method $this")
        }
        setter.invoke(instance, value)
    }

    val propertyType: ResolvedClass
        get() = if (getter == null) {
            val type = TypeToken
                .of(javaField!!.fieldType.underlying)
                .wrap()
                .type
            TypeResolver.resolve(type)
        } else {
            val type = TypeToken
                .of(getter.returnType.underlying)
                .wrap()
                .type
            TypeResolver.resolve(type)
        }

    val isWriteable: Boolean = setter != null

    override val declaredAnnotations: Array<Annotation> by lazy {
        val fieldAnn =
            if (javaField == null) emptyArray()
            else {
                javaField.declaredAnnotations as Array<Annotation>
            }

        val getterAnn =
            if (getter == null) emptyArray()
            else {
                getter.declaredAnnotations as Array<Annotation>
            }

        val setterAnn =
            if (setter == null) emptyArray()
            else {
                setter.declaredAnnotations as Array<Annotation>
            }

        fieldAnn + getterAnn + setterAnn
    }

    override val annotations: Array<Annotation> by lazy {
        val fieldAnn =
            if (javaField == null) emptyArray()
            else {
                javaField.annotations as Array<Annotation>
            }

        val getterAnn =
            if (getter == null) emptyArray()
            else {
                getter.annotations as Array<Annotation>
            }

        val setterAnn =
            if (setter == null) emptyArray()
            else {
                setter.annotations as Array<Annotation>
            }

        fieldAnn + getterAnn + setterAnn
    }

    override fun toString(): String {
        val mode = if (isWriteable) "write $name" else "read $name"
        return "$mode: $propertyType"
    }
}