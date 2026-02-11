package com.anjunar.technologyspeaks.security

import com.anjunar.json.mapper.intermediate.model.JsonArray
import com.anjunar.json.mapper.intermediate.model.JsonObject
import com.anjunar.technologyspeaks.security.WebAuthnManagerProvider.ORIGIN
import com.anjunar.technologyspeaks.security.WebAuthnManagerProvider.RP_ID
import com.anjunar.technologyspeaks.security.WebAuthnManagerProvider.RP_NAME
import com.anjunar.technologyspeaks.security.WebAuthnManagerProvider.challengeStore
import com.anjunar.technologyspeaks.security.WebAuthnManagerProvider.webAuthnManager
import com.webauthn4j.data.PublicKeyCredentialParameters
import com.webauthn4j.data.PublicKeyCredentialType
import com.webauthn4j.data.RegistrationParameters
import com.webauthn4j.data.attestation.statement.COSEAlgorithmIdentifier
import com.webauthn4j.data.client.Origin
import com.webauthn4j.data.client.challenge.DefaultChallenge
import com.webauthn4j.server.ServerProperty
import com.webauthn4j.util.Base64UrlUtil
import jakarta.annotation.security.RolesAllowed
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.security.SecureRandom
import java.util.*


@Suppress("UNCHECKED_CAST")
@RestController
class WebAuthnRegisterController(val store: CredentialStore, val registerService: RegisterService) {

    @PostMapping("/security/register/options", produces = ["application/json"], consumes = ["application/json"])
    @RolesAllowed("Anonymous")
    fun options(@RequestBody body: JsonObject): JsonObject {
        val username = body.getString("email")

        val challengeBytes = ByteArray(32)
        SecureRandom().nextBytes(challengeBytes)
        val challenge = DefaultChallenge(challengeBytes)
        challengeStore.put(username, challenge)

        val credentials = store.loadByUsername(username)

        val excludeCredentials = credentials
            .map { it ->
                JsonObject()
                    .put("type", "public-key")
                    .put("id", store.credentialId(it))
            }
            .toList()

        return JsonObject()
            .put("challenge", Base64UrlUtil.encodeToString(challengeBytes))
            .put(
                "rp", JsonObject()
                    .put("name", RP_NAME)
                    .put("id", RP_ID)
            )
            .put(
                "user", JsonObject()
                    .put("id", Base64UrlUtil.encodeToString(username.toByteArray()))
                    .put("name", username)
                    .put("displayName", username)
            )
            .put(
                "pubKeyCredParams", JsonArray()
                    .add(JsonObject().put("type", "public-key").put("alg", -7))
                    .add(JsonObject().put("type", "public-key").put("alg", -257))
            )
            .put(
                "authenticatorSelection", JsonObject()
                    .put("userVerification", "discouraged")
                    .put("requireResidentKey", false)
            )
            .put("attestation", "none")
            .put("timeout", 60000)
            .put("excludeCredentials", JsonArray(ArrayList(excludeCredentials)))
    }

    @PostMapping("/security/register/finish", produces = ["application/json"], consumes = ["application/json"])
    @RolesAllowed("Anonymous")
    fun finish(@RequestBody body: JsonObject): JsonObject {
        val publicKeyCredential = body.getJsonObject("optionsJSON")
        val credentialId = publicKeyCredential.getString("id")
        val username = body.getString("email")
        val nickName = body.getString("nickName")

        val registrationData = webAuthnManager.parseRegistrationResponseJSON(publicKeyCredential.encode())
        val challenge = challengeStore.get(username)

        val serverProperty = ServerProperty(Origin(ORIGIN), RP_ID, challenge)
        val pubKeyCredParams = Arrays.asList(
            PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY, COSEAlgorithmIdentifier.ES256),
            PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY, COSEAlgorithmIdentifier.RS256)
        )
        val registrationParameters = RegistrationParameters(
            serverProperty,
            pubKeyCredParams,
            false,
            true
        )

        try {
            webAuthnManager.verify(registrationData, registrationParameters)

            val webAuthnCredentialRecord = WebAuthnCredentialRecord(
                username,
                registrationData.attestationObject,
                registrationData.collectedClientData,
                registrationData.clientExtensions,
                registrationData.transports
            )

            val secure = SecureRandom()
            val n = secure.nextInt(1_000_000)
            val code = String.format("$n%06d", n)

            registerService.register(username, code, nickName)

            store.saveRecord(username, nickName, code, webAuthnCredentialRecord)

            return JsonObject()
                .put("status", "success")
                .put("credentialId", credentialId)

        } catch (ex: Exception) {
            return JsonObject()
                .put("status", "error")
                .put("message", ex.message!!)
        }


    }

}