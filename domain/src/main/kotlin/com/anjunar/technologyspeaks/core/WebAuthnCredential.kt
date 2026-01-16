package com.anjunar.technologyspeaks.core

import com.anjunar.technologyspeaks.SpringContext
import com.anjunar.technologyspeaks.hibernate.EntityContext
import com.anjunar.technologyspeaks.hibernate.RepositoryContext
import jakarta.persistence.Entity
import java.util.UUID

@Entity
class WebAuthnCredential(
    var credentialId: String,

    var publicKey: ByteArray,

    var publicKeyAlgorithm: Long,

    var counter: Long,

    var aaguid: UUID,

    var code: String
) : Credential(), EntityContext<WebAuthnCredential> {

    companion object : RepositoryContext<WebAuthnCredential>() {

        fun loadByCredentialId(credentialId: String) : WebAuthnCredential? {
            val entityManager = SpringContext.entityManager()

            return entityManager.createQuery("from WebAuthnCredential c where c.credentialId = :credentialId", WebAuthnCredential::class.java)
                .setParameter("credentialId", credentialId)
                .singleResultOrNull
        }

        fun findByEmail(email: String): List<WebAuthnCredential> {
            val entityManager = SpringContext.entityManager()

            return entityManager.createQuery("select c from WebAuthnCredential c join c.email e where e.value = :email and type(c) = WebAuthnCredential", WebAuthnCredential::class.java)
                .setParameter("email", email)
                .resultList
        }



    }

}