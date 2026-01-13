package jFx2.forms

typealias Disposable = () -> Unit

interface FormRegistryScope {
    fun register(qName: String, field: Any): Disposable
    fun resolveOrNull(qName: String): Any?
}

class RootFormRegistry : FormRegistryScope {
    private val fields = LinkedHashMap<String, Any>()

    override fun register(qName: String, field: Any): Disposable {
        fields[qName] = field
        return { fields.remove(qName) }
    }

    override fun resolveOrNull(qName: String): Any? = fields[qName]
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
}