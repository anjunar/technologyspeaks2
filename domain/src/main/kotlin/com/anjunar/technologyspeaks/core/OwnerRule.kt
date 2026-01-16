package com.anjunar.technologyspeaks.core

import com.anjunar.json.mapper.provider.OwnerProvider
import com.anjunar.json.mapper.schema.VisibilityRule
import com.anjunar.kotlin.universe.introspector.AbstractProperty
import com.anjunar.technologyspeaks.SpringContext
import com.anjunar.technologyspeaks.security.IdentityHolder
import com.anjunar.technologyspeaks.security.SessionHolder

class OwnerRule<E : OwnerProvider> : VisibilityRule<E> {

    val holder = SpringContext.getBean(IdentityHolder::class)

    override fun isVisible(instance: E, property: AbstractProperty): Boolean {
        return true
    }

    override fun isWriteable(instance: E, property: AbstractProperty): Boolean {
        return holder.user.id == instance.owner().id
    }

}