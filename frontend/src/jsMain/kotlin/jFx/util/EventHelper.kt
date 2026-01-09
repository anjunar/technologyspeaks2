package jFx.util

import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event

object EventHelper {

    fun events(element : HTMLElement, handler: (Event) -> Unit, vararg events: String) {
        for (string in events) {
            element.addEventListener(string, handler)
        }
    }

}