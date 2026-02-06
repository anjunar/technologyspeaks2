package com.anjunar.technologyspeaks.timeline

import com.anjunar.json.mapper.annotations.UseConverter
import com.anjunar.json.mapper.provider.EntityProvider
import com.anjunar.json.mapper.provider.OwnerProvider
import com.anjunar.json.mapper.schema.EntitySchema
import com.anjunar.json.mapper.schema.SchemaProvider
import com.anjunar.technologyspeaks.core.AbstractEntity
import com.anjunar.technologyspeaks.core.AbstractEntitySchema
import com.anjunar.technologyspeaks.core.OwnerRule
import com.anjunar.technologyspeaks.core.User
import com.anjunar.technologyspeaks.hibernate.EntityContext
import com.anjunar.technologyspeaks.hibernate.RepositoryContext
import com.anjunar.technologyspeaks.shared.editor.Node
import com.anjunar.technologyspeaks.shared.editor.NodeType
import com.anjunar.technologyspeaks.shared.likeable.Like
import jakarta.json.bind.annotation.JsonbProperty
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.ManyToOne
import jakarta.persistence.NamedAttributeNode
import jakarta.persistence.NamedEntityGraph
import jakarta.persistence.NamedEntityGraphs
import jakarta.persistence.NamedSubgraph
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.Type

@Entity
@Table(name = "Timeline#Post")
@NamedEntityGraphs(
    value = [
        NamedEntityGraph(
            name = "Post.full",
            subgraphs = [
                NamedSubgraph(
                    "user",
                    User::class,
                    attributeNodes = [
                        NamedAttributeNode("id"),
                        NamedAttributeNode("nickName"),
                        NamedAttributeNode("image"),
                        NamedAttributeNode("info"),
                    ]
                )
            ],
            attributeNodes = [
                NamedAttributeNode("id"),
                NamedAttributeNode("user", "user"),
                NamedAttributeNode("editor"),
                NamedAttributeNode("likes"),
            ]
        )
    ]
)
class Post : AbstractEntity(), EntityContext<Post>, OwnerProvider {

    @ManyToOne(optional = false)
    @JsonbProperty
    lateinit var user: User

    @Column(columnDefinition = "jsonb")
    @Type(NodeType::class)
    @UseConverter
    @JsonbProperty
    lateinit var editor: Node

    @OneToMany(cascade = [CascadeType.ALL])
    @JsonbProperty
    val likes : MutableSet<Like> = HashSet()

    override fun owner(): EntityProvider = user

    companion object : RepositoryContext<Post>(), SchemaProvider {

        class Schema : AbstractEntitySchema<Post>() {
            val user = property(Post::user, OwnerRule())
            val editor = property(Post::editor, OwnerRule())
            val likes = property(Post::likes, OwnerRule())
        }

    }
}