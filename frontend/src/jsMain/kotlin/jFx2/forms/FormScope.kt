package jFx2.forms

import jFx2.core.capabilities.DisposeBag

class DisposeBag {
    private val items = ArrayDeque<Disposable>()
    fun add(d: Disposable) { items.addLast(d) }
    fun dispose() { while (items.isNotEmpty()) runCatching { items.removeLast().invoke() } }
}

interface FormScope {
    val path: String
    val rootRegistry: FormRegistryScope

    fun qualify(name: String): String

    fun child(name: String): FormScope

    fun register(name: String, field: Any): Disposable

    fun resolveOrNull(name: String): Any?

    fun <T : Any> resolveOrNull(name: String, type: kotlin.reflect.KClass<T>): T?

    fun dispose()
}

@Suppress("UNCHECKED_CAST")
class FormScopeImpl(
    override val path: String,
    override val rootRegistry: FormRegistryScope,
    private val bag: DisposeBag = DisposeBag()
) : FormScope {

    override fun qualify(name: String): String {
        // Wenn schon absolut (enthält '.'), unverändert lassen
        if (name.contains('.')) return name
        if (path.isBlank()) return name
        return "$path.$name"
    }

    override fun child(name: String): FormScope {
        val childPath = qualify(name)
        return FormScopeImpl(childPath, rootRegistry, bag)
    }

    override fun register(name: String, field: Any): Disposable {
        val qName = qualify(name)
        rootRegistry.register(qName, field)

        return { rootRegistry.unregister(qName) }
    }

    override fun resolveOrNull(name: String): Any? =
        rootRegistry.resolveOrNull(qualify(name))

    override fun <T : Any> resolveOrNull(name: String, type: kotlin.reflect.KClass<T>): T? {
        val v = resolveOrNull(name) ?: return null
        return (if (type.isInstance(v)) v else null) as T?
    }

    override fun dispose() = bag.dispose()
}