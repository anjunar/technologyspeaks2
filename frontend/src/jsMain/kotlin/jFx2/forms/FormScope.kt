package jFx2.forms

class FormScope(
    val name: String,
    val parent: FormScope? = null
) {
    val path: String =
        if (parent?.path.isNullOrBlank()) name
        else parent!!.path + "." + name

    fun qualify(fieldName: String): String = "$path.$fieldName"
}