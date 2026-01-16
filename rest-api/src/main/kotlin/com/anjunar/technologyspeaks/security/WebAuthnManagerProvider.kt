package com.anjunar.technologyspeaks.security

import com.webauthn4j.WebAuthnManager
import com.webauthn4j.data.client.challenge.Challenge
import java.util.concurrent.ConcurrentHashMap

object WebAuthnManagerProvider {

    val webAuthnManager = WebAuthnManager.createNonStrictWebAuthnManager()

    val challengeStore = ConcurrentHashMap<String, Challenge>()

    var ORIGIN = "http://localhost:4200"

    var RP_ID = "localhost"

    val RP_NAME = "Technology Speaks"


}