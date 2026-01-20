package com.anjunar.technologyspeaks.security

import com.anjunar.json.mapper.intermediate.model.JsonArray
import com.anjunar.json.mapper.intermediate.model.JsonObject
import com.anjunar.technologyspeaks.core.WebAuthnCredential
import com.anjunar.technologyspeaks.security.WebAuthnManagerProvider.ORIGIN
import com.anjunar.technologyspeaks.security.WebAuthnManagerProvider.RP_ID
import com.anjunar.technologyspeaks.security.WebAuthnManagerProvider.challengeStore
import com.anjunar.technologyspeaks.security.WebAuthnManagerProvider.webAuthnManager
import com.webauthn4j.data.AuthenticationParameters
import com.webauthn4j.data.client.Origin
import com.webauthn4j.data.client.challenge.DefaultChallenge
import com.webauthn4j.server.ServerProperty
import com.webauthn4j.util.Base64UrlUtil
import com.webauthn4j.verifier.exception.VerificationException
import jakarta.annotation.security.RolesAllowed
import jakarta.persistence.EntityManager
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.security.SecureRandom

@Suppress("UNCHECKED_CAST")
@RestController
class WebAuthnLoginController(val store: CredentialStore, val entityManager: EntityManager, val identityHolder: SessionHolder) {

    @PostMapping("/security/login/options", produces = ["application/json"], consumes = ["application/json"])
    @RolesAllowed("Anonymous")
    fun options(@RequestBody request: JsonObject): JsonObject {
        val username = request.getString("email")

        val challengeBytes = ByteArray(32)
        SecureRandom().nextBytes(challengeBytes)
        val challenge = DefaultChallenge(challengeBytes)
        challengeStore[username] = challenge

        val credentials = store.loadByUsername(username)

        val allowCredentials = credentials
            .map { it ->
                    JsonObject()
                        .put("type", "public-key")
                        .put("id", store.credentialId(it))
            }
            .toList()

        return JsonObject()
            .put("challenge", Base64UrlUtil.encodeToString(challengeBytes))
            .put("rpId", RP_ID)
            .put("allowCredentials", JsonArray(ArrayList(allowCredentials)))
            .put("userVerification", "discouraged")
            .put("timeout", 60000)
    }

    @PostMapping("/security/login/finish", produces = ["application/json"], consumes = ["application/json"])
    @RolesAllowed("Anonymous")
    fun finish(@RequestBody body : JsonObject) : JsonObject {
        val publicKeyCredential = body.getJsonObject("optionsJSON")
        val username = body.getString("email")
        val credentialId = publicKeyCredential.getString("id")
        if (credentialId.isEmpty()) {
            throw IllegalArgumentException("Credential ID is missing in response")
        }

        val authenticationData = webAuthnManager.parseAuthenticationResponseJSON(publicKeyCredential.encode())

        val credentialRecord = store.loadByCredentialId(credentialId)

        val challenge = challengeStore.get(username)

        @Suppress("DEPRECATION")
        val serverProperty = ServerProperty(Origin(ORIGIN), RP_ID, challenge)
        val authenticationParameters = AuthenticationParameters(
            serverProperty,
            credentialRecord,
            null,
            false,
            true
        )

        try {
            val authenticationData = webAuthnManager.verify(authenticationData, authenticationParameters)

            val entity = entityManager.createQuery("from WebAuthnCredential c join fetch c.roles r join fetch c.email e join fetch e.user where c.credentialId = : credentialId", WebAuthnCredential::class.java)
                .setParameter("credentialId", credentialId)
                .singleResultOrNull

            entity.counter = authenticationData.authenticatorData?.signCount!!

            val user = store.loadUser(credentialId)

            identityHolder.user = user.id
            identityHolder.credentials = entity.id

            return JsonObject()
                .put("status", "success")
                .put("user", user.nickName)



        } catch (ex: VerificationException) {
            return JsonObject()
                .put("status", "error")
                .put("message", ex.message.toString())
        }


    }
}