package app.services

import app.domain.Application
import jFx2.client.JsonClient
import kotlinx.coroutines.*

object ApplicationService {

    lateinit var app: Application

    fun invoke(): Job {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        return scope.launch {
            app = JsonClient.invoke("/service")
        }
    }

}