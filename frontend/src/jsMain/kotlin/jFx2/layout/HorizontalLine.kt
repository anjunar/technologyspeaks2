package jFx2.layout

import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.codegen.JfxComponentBuilder
import jFx2.core.dom.ElementInsertPoint
import org.w3c.dom.HTMLHRElement

@JfxComponentBuilder
class Hr(override val node: HTMLHRElement) : Component<HTMLHRElement>()
