package app.services

import app.domain.Application
import app.domain.core.Data
import app.domain.timeline.Post
import jFx2.client.JsonClient
import jFx2.state.Property
import jFx2.state.Disposable

object ApplicationService {

    val app = Property(Application())

    interface Message

    class MessageBus {
        private val listeners = LinkedHashMap<Int, (Message) -> Unit>()
        private var nextId = 1

        fun publish(message: Message) {
            val snapshot = listeners.values.toList()
            snapshot.forEach { runCatching { it(message) } }
        }

        fun subscribe(listener: (Message) -> Unit): Disposable {
            val id = nextId++
            listeners[id] = listener
            return Disposable { listeners.remove(id) }
        }
    }

    val messageBus = MessageBus()

    suspend fun invoke() {
        app.set(JsonClient.invoke("/service"))
    }

}
