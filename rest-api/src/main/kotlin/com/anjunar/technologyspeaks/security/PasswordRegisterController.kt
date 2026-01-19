package com.anjunar.technologyspeaks.security

import com.anjunar.json.mapper.intermediate.model.JsonObject
import com.anjunar.technologyspeaks.core.EMail
import com.anjunar.technologyspeaks.core.PasswordCredential
import com.anjunar.technologyspeaks.core.Role
import com.anjunar.technologyspeaks.core.User
import jakarta.annotation.security.RolesAllowed
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.security.SecureRandom

@RestController
class PasswordRegisterController(val registerService: RegisterService) {

    @PostMapping("/security/register", produces = ["application/json"], consumes = ["application/json"])
    @RolesAllowed("Anonymous")
    @Transactional
    fun register(@RequestBody jsonObject: JsonObject) : JsonObject {
        val nickName = jsonObject.getString("nickName")
        val email = jsonObject.getString("email")
        val password = jsonObject.getString("password")

        var user = User.query("nickName" to nickName)

        if (user == null) {

            val guestRole = Role.query("name" to "Guest")

            user = User(nickName)

            val emailEntity = EMail(email)
            emailEntity.user = user
            user.emails.add(emailEntity)

            val passwordCredential = PasswordCredential(password)
            passwordCredential.email = emailEntity
            passwordCredential.roles.add(guestRole!!)
            emailEntity.credentials.add(passwordCredential)

            user.persist()

            val secure = SecureRandom()
            val n = secure.nextInt(1_000_000)
            val code = String.format("$n%06d", n)

            registerService.register(email, code, nickName)
            return JsonObject()
                .put("status", "success")


        } else {

            return JsonObject()
                .put("status", "error")
                .put("message", "User already exists")

        }


    }

}