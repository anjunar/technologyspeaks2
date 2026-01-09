package javascriptFx.core

abstract class Producer<B : ElementBuilder<out R>, R> {

    abstract fun createBuilder(): B

    fun apply(
        ref: Ref<B> = Ref(),
        ctx: BuildContext,
        parent: ElementBuilder<*>,
        body: (B, BuildContext) -> Unit
    ): R {
        // Erwartet eine Kotlin-DSL-API, die ref + createBuilder + body entgegennimmt.
        return KotlinDSL.create(ref, createBuilder(), ctx, parent, body)
    }

    fun build(
        ref: Ref<B> = Ref(),
        body: (B, BuildContext) -> Unit
    ): B {
        val ctx = BuildContext()
        return KotlinDSL.createBuilder(ref, createBuilder(), ctx, body)
    }
}
