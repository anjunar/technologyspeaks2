package com.anjunar.technologyspeaks.shared.likeable

import com.anjunar.technologyspeaks.security.IdentityHolder
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LikeService(
    private val identityHolder: IdentityHolder,
    private val entityManager: EntityManager
) {

    @Transactional
    fun toggle(entity: LikeableEntity): Set<Like> {
        val userId = identityHolder.user.id

        val existing = entity.likes.filter { it.user.id == userId }
        if (existing.isNotEmpty()) {
            existing.forEach {
                entity.likes.remove(it)
                entityManager.remove(it)
            }
            return entity.likes
        }

        val like = Like()
        like.user = identityHolder.user
        entityManager.persist(like)
        entity.likes.add(like)

        return entity.likes
    }
}
