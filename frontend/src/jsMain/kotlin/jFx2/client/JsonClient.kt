package jFx2.client

import app.domain.core.AbstractLink
import app.domain.core.UsersLink
import app.domain.security.LogoutLink
import app.domain.security.PasswordLoginLink
import app.domain.security.PasswordRegisterLink
import app.domain.security.WebAuthnLoginLink
import app.domain.security.WebAuthnRegisterLink
import app.domain.time.PostsLink
import jFx2.router.navigate
import jFx2.state.Property
import jFx2.state.PropertySerializer
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.w3c.fetch.Headers
import org.w3c.fetch.RequestInit

object JsonClient {

    suspend inline fun <reified O> invoke(url: String, requestInit: RequestInit = RequestInit()): O {
        val response = window.fetch(url, requestInit).await()

        if (!response.ok) {
            val platformAvailable = js("PublicKeyCredential.isUserVerifyingPlatformAuthenticatorAvailable();")
            if (!platformAvailable) {
                navigate("/security/login")
            } else {
                navigate("/security/login/options")
            }
        }

        val defaultJson = configure()

        return defaultJson.decodeFromString<O>(response.text().await())
    }

    suspend inline fun <reified I, reified O> post(url: String, entity : I): O {
        val headers = Headers()
        headers.set("Content-Type", "application/json")
        headers.set("Accept", "application/json")

        return invoke(url, RequestInit(
            method = "POST",
            headers = headers,
            body = Json.encodeToString(entity)
        ))
    }

    suspend inline fun <reified I, reified O> put(url: String, entity : I): O {
        val headers = Headers()
        headers.set("Content-Type", "application/json")
        headers.set("Accept", "application/json")

        return invoke(url, RequestInit(
            method = "PUT",
            headers = headers,
            body = Json.encodeToString(entity)
        ))
    }

    suspend inline fun <reified I> delete(url: String, entity : I) {
        val headers = Headers()
        headers.set("Content-Type", "application/json")

        val requestInit = RequestInit(
            method = "DELETE",
            headers = headers,
            body = Json.encodeToString(entity)
        )

        val response = window.fetch(url, requestInit).await()

        if (!response.ok) throw RuntimeException("Error ${response.status} ${response.statusText}")

    }

    fun configure(): Json {
        val module = SerializersModule {
            polymorphic(AbstractLink::class) {
                subclass(WebAuthnLoginLink::class)
                subclass(WebAuthnRegisterLink::class)
                subclass(PasswordLoginLink::class)
                subclass(PasswordRegisterLink::class)
                subclass(UsersLink::class)
                subclass(LogoutLink::class)
                subclass(PostsLink::class)
            }
        }

        val defaultJson = Json {
            serializersModule = module
            encodeDefaults = true
            ignoreUnknownKeys = true
            explicitNulls = false
        }
        return defaultJson
    }
}