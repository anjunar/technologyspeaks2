package app.services

import app.domain.Application
import jFx2.client.JsonClient
import jFx2.state.Property
import kotlinx.coroutines.*

object ApplicationService {

    val app = Property(Application())

    suspend fun invoke() {
        app.set(JsonClient.invoke("/service"))
    }

}