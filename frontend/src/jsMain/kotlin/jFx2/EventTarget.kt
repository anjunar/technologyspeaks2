package jFx2

import jFx2.state.Disposable
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventTarget

fun EventTarget.on(type: String, listener: (Event) -> Unit): Disposable {
    addEventListener(type, listener)
    return { removeEventListener(type, listener) }
}