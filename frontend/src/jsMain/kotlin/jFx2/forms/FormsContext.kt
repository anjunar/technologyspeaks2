package jFx2.forms

class FormsContext(
    val rootRegistry: FormRegistryScope,
    val scope: FormScope? = null,
    val effectiveRegistry: FormRegistryScope? = null
)