package jFx2.forms

typealias Disposable = () -> Unit

interface FormRegistryScope {
    fun register(qName: String, field: Any): Disposable
    fun resolveOrNull(qName: String): Any?
    fun unregister(name: String)
    fun getOrNull(name: String): Any?
}

class RootFormRegistry : FormRegistryScope {
    private val fields = LinkedHashMap<String, Any>()

    override fun register(qName: String, field: Any): Disposable {
        fields[qName] = field
        return { fields.remove(qName) }
    }

    override fun unregister(name: String) {
        fields.remove(name)
    }

    override fun resolveOrNull(qName: String): Any? = fields[qName]

    override fun getOrNull(name: String): Any? {
        return fields[name]
    }
}

class NamespacedFormRegistry(
    private val basePath: String,
    private val delegate: FormRegistryScope
) : FormRegistryScope {

    private fun qn(name: String): String =
        if (name.contains('.')) name else "$basePath.$name"

    override fun register(qName: String, field: Any): Disposable =
        delegate.register(qn(qName), field)

    override fun resolveOrNull(qName: String): Any? =
        delegate.resolveOrNull(qn(qName))

    override fun unregister(name: String) {
        delegate.unregister(name)
    }

    override fun getOrNull(name: String): Any? =
        delegate.getOrNull(qn(name))
}