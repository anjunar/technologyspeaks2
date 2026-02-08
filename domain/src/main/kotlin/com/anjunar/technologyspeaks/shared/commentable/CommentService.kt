package com.anjunar.technologyspeaks.shared.commentable

import com.anjunar.technologyspeaks.security.IdentityHolder
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class CommentService(
    private val identityHolder: IdentityHolder,
    private val entityManager: EntityManager
) {

    @Transactional
    fun list(
        targetType: String,
        targetId: UUID,
        index: Int,
        limit: Int,
        createdDesc: Boolean
    ): List<Comment> {
        val cb = entityManager.criteriaBuilder
        val cq = cb.createQuery(Comment::class.java)
        val root = cq.from(Comment::class.java)

        cq.select(root).where(
            cb.and(
                cb.equal(root.get<String>("targetType"), targetType),
                cb.equal(root.get<UUID>("targetId"), targetId),
            )
        ).orderBy(
            if (createdDesc) cb.desc(root.get<Any>("created")) else cb.asc(root.get<Any>("created"))
        )

        return entityManager.createQuery(cq)
            .setFirstResult(index)
            .setMaxResults(limit)
            .resultList
    }

    @Transactional
    fun count(targetType: String, targetId: UUID): Long {
        val cb = entityManager.criteriaBuilder
        val cq = cb.createQuery(Long::class.java)
        val root = cq.from(Comment::class.java)

        cq.select(cb.count(root)).where(
            cb.and(
                cb.equal(root.get<String>("targetType"), targetType),
                cb.equal(root.get<UUID>("targetId"), targetId),
            )
        )

        return entityManager.createQuery(cq).singleResult
    }

    @Transactional
    fun create(targetType: String, targetId: UUID, text: String): Comment {
        val comment = Comment()
        comment.user = identityHolder.user
        comment.targetType = targetType
        comment.targetId = targetId
        comment.text = text

        entityManager.persist(comment)

        return comment
    }
}

