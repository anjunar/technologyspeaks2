package app

import javascriptFx.core.*
import org.w3c.dom.Node
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLInputElement
import kotlinx.browser.document

// --- Parent, der Node-Children aufnehmen kann ---
class DivBuilder : ChildNodeBuilder<HTMLDivElement, Node>, NodeBuilder<HTMLDivElement> {

    override val applyValues = mutableListOf<() -> Unit>()
    override var lifeCycle: LifeCycle = LifeCycle.Build

    override fun build(): HTMLDivElement = document.createElement("div") as HTMLDivElement

    // Minimal: wir halten children nicht wirklich in einer observable list, sondern nur als "Adapter".
    // Wenn du ListProperty schon hast, kannst du hier korrekt verdrahten.
    override val children = javascriptFx.state.ListProperty<ElementBuilder<*>>()   // angenommen vorhanden
    override val fxObservableList = javascriptFx.state.ListProperty<Node>()       // angenommen vorhanden

    private val builtNode: HTMLDivElement
        get() = read(refNode) // siehe weiter unten

    // Trick: wir brauchen Zugriff auf das gebaute DOM-Element.
    // Einfacher: speichere es beim build() in einem Feld.
    private lateinit var node: HTMLDivElement
    private var refNode: HTMLDivElement
        get() = node
        set(value) { node = value }

    override fun add(child: ElementBuilder<*>) {
        // add wird in Hook aufgerufen, also nach build() des childs.
        // Wir wollen das DOM-Child anhängen, sobald wir im Apply/Fertig sind.
        // Da Hook vor Apply kommt, ist "node" schon da.
        val childNode = (child as ElementBuilder<*>).let {
            @Suppress("UNCHECKED_CAST")
            (it as ElementBuilder<Node>)
        }
        // Wichtig: child.build() ist bereits gelaufen, also muss childNode ein echtes Node liefern können.
        // In deiner Architektur liefert create(...) das Node zurück, aber hier haben wir nur Builder.
        // Lösung: ChildBuilder speichert Node direkt in Builder (siehe Button/Input unten).
        if (child is HasBuiltNode<*>) {
            @Suppress("UNCHECKED_CAST")
            val n = (child as HasBuiltNode<Node>).built
            node.appendChild(n)
        }
    }

    override fun registerLayoutListener() {
        // optional: z.B. ResizeObserver etc.
    }
}

// Kleines Interface, damit Parent an das gebaute Node kommt
interface HasBuiltNode<N : Node> { val built: N }

// --- Button ---
class ButtonBuilder : ElementBuilder<HTMLButtonElement>, HasBuiltNode<HTMLButtonElement> {
    override val applyValues = mutableListOf<() -> Unit>()
    override var lifeCycle: LifeCycle = LifeCycle.Build

    override lateinit var built: HTMLButtonElement

    override fun build(): HTMLButtonElement {
        built = document.createElement("button") as HTMLButtonElement
        return built
    }

    var text: String
        get() = read(built.textContent ?: "")
        set(value) = write { built.textContent = value }

    fun onClick(handler: () -> Unit) = write {
        built.onclick = { handler(); null }
    }
}

// --- Input ---
class InputBuilder : ElementBuilder<HTMLInputElement>, HasBuiltNode<HTMLInputElement> {
    override val applyValues = mutableListOf<() -> Unit>()
    override var lifeCycle: LifeCycle = LifeCycle.Build

    override lateinit var built: HTMLInputElement

    override fun build(): HTMLInputElement {
        built = document.createElement("input") as HTMLInputElement
        return built
    }

    var placeholder: String
        get() = read(built.placeholder)
        set(value) = write { built.placeholder = value }

    var value: String
        get() = read(built.value)
        set(v) = write { built.value = v }
}

    object Fx {
    fun div(ref: Ref<DivBuilder> = Ref()) =
        object {
            operator fun invoke(ctx: BuildContext, parent: ElementBuilder<*>, body: DivBuilder.(BuildContext) -> Unit) =
                KotlinDSL.create(ref, DivBuilder(), ctx, parent, body)
        }

    fun button(ref: Ref<ButtonBuilder> = Ref()) =
        object {
            operator fun invoke(ctx: BuildContext, parent: ElementBuilder<*>, body: ButtonBuilder.(BuildContext) -> Unit) =
                KotlinDSL.create(ref, ButtonBuilder(), ctx, parent, body)
        }

    fun input(ref: Ref<InputBuilder> = Ref()) =
        object {
            operator fun invoke(ctx: BuildContext, parent: ElementBuilder<*>, body: InputBuilder.(BuildContext) -> Unit) =
                KotlinDSL.create(ref, InputBuilder(), ctx, parent, body)
        }
}
