package com.anjunar.technologyspeaks.core

import com.anjunar.technologyspeaks.hibernate.EntityContext
import jakarta.persistence.Entity
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "Core#ManagedProperty")
class ManagedProperty(var name : String, var visibleForAll : Boolean) : AbstractEntity(), EntityContext<ManagedProperty> {

    @ManyToMany
    val users : MutableSet<User> = HashSet()

    @ManyToOne(optional = false)
    lateinit var view : EntityView


}