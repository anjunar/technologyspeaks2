package jFx.layout

import jFx.core.AbstractChildrenComponent
import kotlinx.browser.document
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

abstract class AbstractBox(private val className: String) : AbstractChildrenComponent<HTMLDivElement, HTMLElement>() {


    val node by lazy {
        val divElement = document.createElement("div") as HTMLDivElement
        if (className.isNotEmpty()) divElement.classList.add()
        divElement
    }

}

