package com.anjunar.technologyspeaks.timeline

import com.anjunar.json.mapper.intermediate.model.JsonObject
import com.anjunar.technologyspeaks.rest.EntityGraph
import com.anjunar.technologyspeaks.rest.types.Data
import com.anjunar.technologyspeaks.rest.types.DTOList
import com.anjunar.technologyspeaks.rest.types.Table
import com.anjunar.technologyspeaks.security.IdentityHolder
import com.anjunar.technologyspeaks.security.LinkBuilder
import com.anjunar.technologyspeaks.shared.commentable.Comment
import com.anjunar.technologyspeaks.shared.commentable.CommentSearch
import com.anjunar.technologyspeaks.shared.commentable.CommentService
import com.anjunar.technologyspeaks.shared.likeable.Like
import com.anjunar.technologyspeaks.shared.likeable.LikeService
import jakarta.annotation.security.RolesAllowed
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*

@RestController
class PostController(
    val identityHolder: IdentityHolder,
    val likeService: LikeService,
    val commentService: CommentService
) {

    @GetMapping(value = ["/timeline/posts/post/{id}"], produces = ["application/json"])
    @RolesAllowed("User", "Administrator")
    @Transactional
    @EntityGraph("Post.full")
    fun read(@PathVariable("id") post: Post): Data<Post> {
        val data = Data(post, Post.schema())

        data.addLinks(
            LinkBuilder.create(PostController::like)
                .withVariable("id", post.id)
                .build()
        )

        data.addLinks(
            LinkBuilder.create(PostController::comments)
                .withVariable("id", post.id)
                .build(),
            LinkBuilder.create(PostController::comment)
                .withVariable("id", post.id)
                .build()
        )

        if (post.user == identityHolder.user) {
            data.addLinks(
                LinkBuilder.create(PostController::read)
                    .withVariable("id", post.id)
                    .build(),
                LinkBuilder.create(PostController::update)
                    .build(),
                LinkBuilder.create(PostController::delete)
                    .withVariable("id", post.id)
                    .build()
            )
        }

        return data
    }

    @PostMapping(value = ["/timeline/posts/post"], produces = ["application/json"], consumes = ["application/json"])
    @RolesAllowed("User", "Administrator")
    @Transactional
    @EntityGraph("Post.full")
    fun save(@RequestBody post: Post): Data<Post> {
        post.user = identityHolder.user
        post.persist()
        val data = Data(post, Post.schema())

        data.addLinks(
            LinkBuilder.create(PostController::like)
                .withVariable("id", post.id)
                .build()
        )

        data.addLinks(
            LinkBuilder.create(PostController::comments)
                .withVariable("id", post.id)
                .build(),
            LinkBuilder.create(PostController::comment)
                .withVariable("id", post.id)
                .build()
        )

        if (post.user == identityHolder.user) {
            data.addLinks(
                LinkBuilder.create(PostController::read)
                    .withVariable("id", post.id)
                    .build(),
                LinkBuilder.create(PostController::update)
                    .build(),
                LinkBuilder.create(PostController::delete)
                    .withVariable("id", post.id)
                    .build()
            )
        }

        return data
    }

    @PutMapping(value = ["/timeline/posts/post"], produces = ["application/json"], consumes = ["application/json"])
    @RolesAllowed("User", "Administrator")
    @Transactional
    @EntityGraph("Post.full")
    fun update(@RequestBody post: Post): Data<Post> {
        val data = Data(post.merge(), Post.schema())

        data.addLinks(
            LinkBuilder.create(PostController::like)
                .withVariable("id", post.id)
                .build()
        )

        data.addLinks(
            LinkBuilder.create(PostController::comments)
                .withVariable("id", post.id)
                .build(),
            LinkBuilder.create(PostController::comment)
                .withVariable("id", post.id)
                .build()
        )

        if (post.user == identityHolder.user) {
            data.addLinks(
                LinkBuilder.create(PostController::read)
                    .withVariable("id", post.id)
                    .build(),
                LinkBuilder.create(PostController::update)
                    .build(),
                LinkBuilder.create(PostController::delete)
                    .withVariable("id", post.id)
                    .build()
            )
        }

        return data
    }

    @PostMapping(value = ["/timeline/posts/post/{id}/like"], produces = ["application/json"])
    @RolesAllowed("User", "Administrator")
    @Transactional
    fun like(@PathVariable("id") post: Post): DTOList<Like> {
        val likes = likeService.toggle(post)
        return DTOList(likes)
    }

    @GetMapping(value = ["/timeline/posts/post/{id}/comments"], produces = ["application/json"])
    @RolesAllowed("User", "Administrator")
    @Transactional
    fun comments(
        @PathVariable("id") post: Post,
        search: CommentSearch
    ): Table<CommentRow> {
        val targetType = Post::class.java.name

        val createdDesc = search.sort.any { raw ->
            val t = raw.trim()
            when {
                t.startsWith("-created") -> true
                t.startsWith("+created") -> false
                t.startsWith("created:", ignoreCase = true) -> t.substringAfter(':').trim().equals("desc", true)
                else -> false
            }
        }

        val entities = commentService
            .list(targetType, post.id, search.index, search.limit, createdDesc)
            .map { CommentRow(it) }

        val count = commentService.count(targetType, post.id)

        return Table(entities, count, Comment.schema())
    }

    @PostMapping(value = ["/timeline/posts/post/{id}/comment"], produces = ["application/json"], consumes = ["application/json"])
    @RolesAllowed("User", "Administrator")
    @Transactional
    fun comment(
        @PathVariable("id") post: Post,
        @RequestBody body: JsonObject
    ): Data<Comment> {
        val text = (body.value["text"]?.value as? String).orEmpty().trim()
        require(text.isNotBlank()) { "text must not be blank" }

        val targetType = Post::class.java.name
        val comment = commentService.create(targetType, post.id, text)
        return Data(comment, Comment.schema())
    }

    @DeleteMapping(value = ["/timeline/posts/post/{id}"])
    @RolesAllowed("User", "Administrator")
    @Transactional
    @EntityGraph("Post.full")
    fun delete(@PathVariable("id") post: Post) {
        post.remove()
    }

    companion object {
        class CommentRow(data: Comment) : Data<Comment>(data, Comment.schema())
    }

}
