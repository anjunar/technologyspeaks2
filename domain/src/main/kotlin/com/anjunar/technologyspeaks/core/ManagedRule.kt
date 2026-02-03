package com.anjunar.technologyspeaks.core

import com.anjunar.json.mapper.provider.EntityProvider
import com.anjunar.json.mapper.provider.OwnerProvider
import com.anjunar.json.mapper.schema.VisibilityRule
import com.anjunar.kotlin.universe.introspector.AbstractProperty
import com.anjunar.technologyspeaks.SpringContext
import com.anjunar.technologyspeaks.security.IdentityHolder
import com.anjunar.technologyspeaks.security.SessionHolder

class ManagedRule<E> : VisibilityRule<E> where E : OwnerProvider, E : EntityProvider {

    val holder = SpringContext.getBean(IdentityHolder::class)

    override fun isVisible(instance: E?, property: AbstractProperty): Boolean {

        if (instance == null) return false

        val owner = User.find(instance.owner().id) ?: return false

        if (holder.user.id == owner.id) return true

        var entityView = User.findViewByUser(owner)

        if (entityView == null) {
            entityView = User.Companion.View()
            entityView.user = owner
            entityView.persist()
        }

        var managedProperty = entityView.properties.find { it.name == property.name }

        if (managedProperty == null) {
            managedProperty = ManagedProperty(property.name, false)
            managedProperty.view = entityView
            entityView.properties.add(managedProperty)
            managedProperty.persist()
        }

        if (managedProperty.visibleForAll) {
            return true
        }

        return managedProperty.users.any { it.id == holder.user.id }
    }

    override fun isWriteable(instance: E?, property: AbstractProperty): Boolean {
        if (instance?.version == -1L) return true

        return holder.user.id == instance!!.owner().id
    }
}