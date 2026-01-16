package com.anjunar.technologyspeaks.security

import com.anjunar.technologyspeaks.core.EMail
import com.anjunar.technologyspeaks.core.Role
import com.anjunar.technologyspeaks.core.User
import com.anjunar.technologyspeaks.core.WebAuthnCredential
import com.webauthn4j.credential.CredentialRecord
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CredentialStore {

    fun credentialId(record: CredentialRecord): String = (record as WebAuthnCredentialRecord).credentialID

    fun loadUser(credentialId: String): User {
        return WebAuthnCredential.loadByCredentialId(credentialId)?.email?.user!!
    }

    @Transactional
    fun saveRecord(email: String, nickName: String, code: String, record: WebAuthnCredentialRecord) {
        val roleAction = Role.query("name" to "Guest")
        val eMail = EMail.query("value" to email)

        val targetEmailFuture: EMail =
            if (eMail == null) {
                val mail = EMail(email)
                val user = User(nickName)

                user.emails.add(mail)
                mail.user = user
                user.persist()
                mail
            } else {
                eMail
            }

        val requiredPersistedData = record.requiredPersistedData
        val credential = WebAuthnCredential(
            requiredPersistedData.credentialId(),
            requiredPersistedData.publicKey(),
            requiredPersistedData.publicKeyAlgorithm(),
            requiredPersistedData.counter(),
            requiredPersistedData.aaguid(),
            code
        )

        credential.roles.add(roleAction!!)
        credential.email = targetEmailFuture

        credential.persist()

    }

    fun loadByUsername(username: String): List<CredentialRecord> {
        val entities = WebAuthnCredential.findByEmail(username)
        return entities
            .map { entity ->
                WebAuthnCredentialRecord.fromRequiredPersistedData(
                    WebAuthnCredentialRecord.RequiredPersistedData(
                        username,
                        entity.credentialId,
                        entity.aaguid,
                        entity.publicKey,
                        entity.publicKeyAlgorithm,
                        entity.counter
                    )
                )
            }
            .toList()
    }

    fun loadByCredentialId(credentialId: String): CredentialRecord {
        val entity = WebAuthnCredential.loadByCredentialId(credentialId)!!
        return WebAuthnCredentialRecord.fromRequiredPersistedData(
            WebAuthnCredentialRecord.RequiredPersistedData(
                entity.email.value,
                entity.credentialId,
                entity.aaguid,
                entity.publicKey,
                entity.publicKeyAlgorithm,
                entity.counter
            )
        )

    }

}