package com.anjunar.technologyspeaks.timeline

import com.anjunar.json.mapper.annotations.UseConverter
import com.anjunar.json.mapper.schema.EntitySchema
import com.anjunar.json.mapper.schema.SchemaProvider
import com.anjunar.technologyspeaks.core.AbstractEntity
import com.anjunar.technologyspeaks.core.User
import com.anjunar.technologyspeaks.hibernate.RepositoryContext
import com.anjunar.technologyspeaks.shared.editor.Node
import com.anjunar.technologyspeaks.shared.editor.NodeType
import com.anjunar.technologyspeaks.shared.likeable.Like
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.Type

@Entity
@Table(name = "Timeline#Post")
class Post : AbstractEntity() {

    @ManyToOne(optional = false)
    lateinit var user: User

    @Column(columnDefinition = "jsonb")
    @Type(NodeType::class)
    @UseConverter
    lateinit var editor: Node

    @OneToMany(cascade = [CascadeType.ALL])
    val likes : MutableSet<Like> = HashSet()

    companion object : RepositoryContext<Post>(), SchemaProvider {

        class Schema : EntitySchema<Post>() {
            val user = property(Post::user)
            val editor = property(Post::editor)
            val likes = property(Post::likes)
        }

    }
}