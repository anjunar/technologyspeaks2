package com.anjunar.technologyspeaks.core

import com.anjunar.json.mapper.provider.EntityProvider
import com.anjunar.json.mapper.provider.OwnerProvider
import com.anjunar.json.mapper.schema.VisibilityRule
import com.anjunar.kotlin.universe.introspector.AbstractProperty
import com.anjunar.technologyspeaks.SpringContext
import com.anjunar.technologyspeaks.security.IdentityHolder
import com.anjunar.technologyspeaks.security.SessionHolder

class OwnerRule<E> : VisibilityRule<E> where E : OwnerProvider, E : EntityProvider {

    val holder = SpringContext.getBean(IdentityHolder::class)

    override fun isVisible(instance: E?, property: AbstractProperty): Boolean {
        return true
    }

    override fun isWriteable(instance: E?, property: AbstractProperty): Boolean {
        if (instance?.version == -1L) {
            return true
        }
        return holder.user.id == instance!!.owner().id
    }

}