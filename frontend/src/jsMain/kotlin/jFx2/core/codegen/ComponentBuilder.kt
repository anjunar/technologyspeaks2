package jFx2.core.codegen

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class JfxComponentBuilder(
    /**
     * Optional explicit builder function name.
     * Defaults to the class name with a lower-cased first character (e.g. `LoggedInUser` -> `loggedInUser`).
     */
    val name: String = "",
    /**
     * HTML tag name used for `scope.create(...)`.
     * If empty, the processor will try to derive it from the `node` constructor parameter type.
     */
    val tag: String = "",
    /**
     * CSS classes to add to the created element.
     */
    val classes: Array<String> = [],
    /**
     * How to invoke `afterBuild()` (if present on the component).
     */
    val afterBuild: AfterBuildMode = AfterBuildMode.SCHEDULED
)

enum class AfterBuildMode {
    NONE,
    SCHEDULED,
    EAGER
}

