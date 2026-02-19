package com.anjunar.technologyspeaks.documents

import com.anjunar.json.mapper.provider.EntityProvider
import com.anjunar.json.mapper.provider.OwnerProvider
import com.anjunar.json.mapper.schema.SchemaProvider
import com.anjunar.technologyspeaks.core.AbstractEntity
import com.anjunar.technologyspeaks.core.AbstractEntitySchema
import com.anjunar.technologyspeaks.core.Media
import com.anjunar.technologyspeaks.core.OwnerRule
import com.anjunar.technologyspeaks.core.User
import com.anjunar.technologyspeaks.core.UserInfo
import com.anjunar.technologyspeaks.hibernate.EntityContext
import com.anjunar.technologyspeaks.hibernate.RepositoryContext
import com.anjunar.technologyspeaks.shared.editor.Node
import com.anjunar.technologyspeaks.shared.editor.NodeType
import jakarta.json.bind.annotation.JsonbProperty
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.ManyToOne
import jakarta.persistence.NamedAttributeNode
import jakarta.persistence.NamedEntityGraph
import jakarta.persistence.NamedEntityGraphs
import jakarta.persistence.NamedSubgraph
import jakarta.persistence.Table
import org.hibernate.annotations.Type

@Entity
@Table(name = "Documents#Document")
@NamedEntityGraphs(
    value = [
        NamedEntityGraph(
            name = "Document.full",
            subgraphs = [
                NamedSubgraph(
                    name = "image",
                    type = Media::class,
                    attributeNodes = [
                        NamedAttributeNode("id")
                    ]
                ),
                NamedSubgraph(
                    name = "userInfo",
                    type = UserInfo::class,
                    attributeNodes = [
                        NamedAttributeNode("id"),
                        NamedAttributeNode("firstName"),
                        NamedAttributeNode("lastName")
                    ]
                ),
                NamedSubgraph(
                    name = "user",
                    type = User::class,
                    attributeNodes = [
                        NamedAttributeNode("id"),
                        NamedAttributeNode("nickName"),
                        NamedAttributeNode("image", subgraph = "image"),
                        NamedAttributeNode("info", subgraph = "userInfo")
                    ]
                )
            ],
            attributeNodes = [
                NamedAttributeNode("id"),
                NamedAttributeNode("modified"),
                NamedAttributeNode("created"),
                NamedAttributeNode("user", subgraph = "user"),
                NamedAttributeNode("editor"),
                NamedAttributeNode("title")
            ]
        )
    ]
)
class Document(
    @Column(nullable = false)
    @JsonbProperty
    var title: String
) : AbstractEntity(), EntityContext<Document>, OwnerProvider {

    @ManyToOne(optional = false)
    @JsonbProperty
    lateinit var user: User

    @Column(columnDefinition = "jsonb")
    @Type(NodeType::class)
    @JsonbProperty
    lateinit var editor: Node

    override fun owner(): EntityProvider = user

    companion object : RepositoryContext<Document>(), SchemaProvider {

        class Schema : AbstractEntitySchema<Document>() {
            @JsonbProperty val title = property(Document::title, OwnerRule())
            @JsonbProperty val user = property(Document::user, OwnerRule())
            @JsonbProperty val editor = property(Document::editor, OwnerRule())
        }

    }

}