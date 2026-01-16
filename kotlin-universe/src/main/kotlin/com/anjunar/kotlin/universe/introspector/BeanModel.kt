package com.anjunar.kotlin.universe.introspector

import com.anjunar.kotlin.universe.ResolvedClass
import com.anjunar.kotlin.universe.annotations.Annotated
import com.anjunar.kotlin.universe.members.ResolvedField
import com.anjunar.kotlin.universe.members.ResolvedMethod
import java.util.regex.Matcher
import java.util.regex.Pattern

class BeanModel(val underlying: ResolvedClass) : Annotated {

    private val getterRegex: Pattern = Pattern.compile("(?:is|get)(\\w+)")

    val declaredProperties: Array<BeanProperty> by lazy {
        underlying.declaredMethods
            .filter { method ->
                val matcher: Matcher = getterRegex.matcher(method.name)
                matcher.matches() && method.parameters.isEmpty()
            }
            .map { getterMethod ->
                val matcher = getterRegex.matcher(getterMethod.name)
                if (matcher.matches()) {
                    val group = matcher.group(1)
                    val propertyName =
                        group.substring(0, 1).lowercase() + group.substring(1)

                    val field = underlying.declaredFields
                        .firstOrNull { field: ResolvedField -> field.name == propertyName }

                    val setterMethod = underlying.declaredMethods
                        .firstOrNull { method: ResolvedMethod ->
                            method.name == "set$group" && method.parameters.size == 1
                        }

                    BeanProperty(this, propertyName, field, getterMethod, setterMethod)
                } else {
                    throw IllegalStateException("no getter found ${getterMethod.name}")
                }
            }
            .toTypedArray()
    }

    val properties: Array<BeanProperty> by lazy {
        underlying.methods
            .filter { method ->
                val matcher: Matcher = getterRegex.matcher(method.name)
                matcher.matches() && method.parameters.isEmpty()
            }
            .map { getterMethod ->
                val matcher = getterRegex.matcher(getterMethod.name)
                if (matcher.matches()) {
                    val group = matcher.group(1)
                    val propertyName =
                        group.substring(0, 1).lowercase() + group.substring(1)

                    val field = underlying.fields
                        .firstOrNull { field: ResolvedField -> field.name == propertyName }

                    val setterMethod = underlying.methods
                        .firstOrNull { method: ResolvedMethod ->
                            method.name == "set$group" && method.parameters.size == 1
                        }

                    BeanProperty(this, propertyName, field, getterMethod, setterMethod)
                } else {
                    throw IllegalStateException("no getter found ${getterMethod.name}")
                }
            }
            .toTypedArray()
    }

    fun findDeclaredProperty(name: String): BeanProperty? =
        declaredProperties.firstOrNull { it.name == name }

    fun findProperty(name: String): BeanProperty? =
        properties.firstOrNull { it.name == name }

    override val declaredAnnotations: Array<Annotation> by lazy {
        underlying.declaredAnnotations
    }

    override val annotations: Array<Annotation> by lazy {
        underlying.annotations
    }
}