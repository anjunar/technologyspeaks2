package com.anjunar.technologyspeaks.security

import com.anjunar.json.mapper.intermediate.model.JsonObject
import com.anjunar.technologyspeaks.core.EMail
import com.anjunar.technologyspeaks.core.PasswordCredential
import com.anjunar.technologyspeaks.core.User
import jakarta.annotation.security.RolesAllowed
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class PasswordLoginController(val sessionHolder: SessionHolder) {

    @PostMapping("/security/login", produces = ["application/json"], consumes = ["application/json"])
    @RolesAllowed("Anonymous")
    fun login(@RequestBody jsonObject: JsonObject) : JsonObject {

        val email = jsonObject.getString("email")
        val password = jsonObject.getString("password")

        val eMailEntity = EMail.query("value" to email)

        if (eMailEntity == null) {
            return JsonObject()
                .put("status", "error")
                .put("message", "User not found")
        } else {
            val credential = eMailEntity
                .credentials
                .filterIsInstance<PasswordCredential>()
                .find { credential -> credential.password == password }

            if (credential != null) {

                sessionHolder.user = eMailEntity.user.id
                sessionHolder.credentials = credential.id

                return JsonObject()
                    .put("status", "success")

            } else {
                return JsonObject()
                    .put("status", "error")
                    .put("message", "Invalid password")
            }

        }
    }



}