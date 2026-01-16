package com.anjunar.technologyspeaks.core

import jakarta.persistence.Entity
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "Core#EntityView")
class EntityView : AbstractEntity() {

    @ManyToOne(optional = false)
    lateinit var user : User

    @OneToMany
    val properties : MutableSet<ManagedProperty> = HashSet()


}