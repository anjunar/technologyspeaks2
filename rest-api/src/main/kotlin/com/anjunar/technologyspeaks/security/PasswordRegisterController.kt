package com.anjunar.technologyspeaks.security

import com.anjunar.json.mapper.intermediate.model.JsonObject
import com.anjunar.technologyspeaks.core.EMail
import com.anjunar.technologyspeaks.core.PasswordCredential
import jakarta.annotation.security.RolesAllowed
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class PasswordRegisterController(val registerService: RegisterService) {

    @PostMapping("/security/register", produces = ["application/json"], consumes = ["application/json"])
    @RolesAllowed("Anonymous")
    fun register(@RequestBody jsonObject: JsonObject) : JsonObject {
        val nickName = jsonObject.getString("nickName")
        val email = jsonObject.getString("email")
        val password = jsonObject.getString("password")

        val emailEntity = EMail.query("value" to email)

        emailEntity?.credentials?.add(PasswordCredential(password))

        registerService.register(nickName, email, password)
        return JsonObject()
            .put("status", "success")
    }

}