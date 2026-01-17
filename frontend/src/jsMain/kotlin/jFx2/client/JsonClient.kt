package jFx2.client

import app.domain.core.Link
import app.domain.security.LoginLink
import app.domain.security.RegisterLink
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.w3c.fetch.RequestInit

object JsonClient {

    suspend inline fun <reified E> invoke(url: String, requestInit: RequestInit = RequestInit()): E {
        val response = window.fetch(url, requestInit).await()

        val module = SerializersModule {
            polymorphic(Link::class) {
                subclass(LoginLink::class)
                subclass(RegisterLink::class)
            }
        }

        val defaultJson = Json {
            serializersModule = module
            encodeDefaults = true
            ignoreUnknownKeys = true
        }

        return defaultJson.decodeFromString<E>(response.text().await())
    }

}