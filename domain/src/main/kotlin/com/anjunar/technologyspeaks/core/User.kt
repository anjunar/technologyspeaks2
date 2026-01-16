package com.anjunar.technologyspeaks.core

import com.anjunar.json.mapper.provider.EntityProvider
import com.anjunar.json.mapper.provider.OwnerProvider
import com.anjunar.json.mapper.schema.SchemaProvider
import com.anjunar.technologyspeaks.SpringContext
import com.anjunar.technologyspeaks.hibernate.EntityContext
import com.anjunar.technologyspeaks.hibernate.RepositoryContext
import jakarta.json.bind.annotation.JsonbProperty
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Entity
@Table(name = "Core#User")
@NamedEntityGraphs(
    value = [
        NamedEntityGraph(
            name = "User.full",
            attributeNodes = [
                NamedAttributeNode("id"),
                NamedAttributeNode("nickName"),
                NamedAttributeNode("image"),
                NamedAttributeNode("info"),
                NamedAttributeNode("address"),
                NamedAttributeNode("emails")
            ]
        )
    ]
)
class User(@JsonbProperty @NotBlank @Size(min = 2, max = 80) var nickName: String)
    : AbstractEntity(), EntityContext<User>, OwnerProvider {

    @ManyToOne(cascade = [CascadeType.ALL])
    @JsonbProperty
    var image: Media? = null

    @OneToOne(cascade = [CascadeType.ALL])
    @JsonbProperty
    var info: UserInfo? = null

    @OneToOne(cascade = [CascadeType.ALL])
    @JsonbProperty
    var address: Address? = null

    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, mappedBy = "user")
    @JsonbProperty
    val emails : MutableSet<EMail> = HashSet()

    override fun owner(): EntityProvider {
        return this
    }

    companion object : SchemaProvider, RepositoryContext<User>() {

        open class Schema : AbstractEntitySchema<User>() {

            @JsonbProperty val nickName = property(User::nickName, OwnerRule())
            @JsonbProperty val image = property(User::image, OwnerRule())
            @JsonbProperty val info = property(User::info, OwnerRule())
            @JsonbProperty val address = property(User::address, OwnerRule())
            @JsonbProperty val emails = property(User::emails, ManagedRule())

        }

        @Entity(name = "UserView")
        class View : EntityView(), EntityContext<View>

        fun findViewByUser(user : User) : EntityView? {
            val entityManager = SpringContext.entityManager()
            return entityManager.createQuery("select v from UserView v left join fetch v.properties where v.user = :user", View::class.java)
                .setParameter("user", user)
                .singleResultOrNull
        }

    }

}