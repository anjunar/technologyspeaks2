package jFx2.codegen

import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.Nullability
import com.google.devtools.ksp.symbol.Variance
import com.google.devtools.ksp.validate
import java.io.OutputStreamWriter

class JfxComponentBuilderProcessor(
    private val env: SymbolProcessorEnvironment
) : SymbolProcessor {

    private val annotationFqName = "jFx2.core.codegen.JfxComponentBuilder"

    private val processed = HashSet<String>()
    private val debug = env.options["jfx2.debug"] == "true"

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(annotationFqName).toList()

        val deferred = ArrayList<KSAnnotated>()

        for (symbol in symbols) {
            val klass = symbol as? KSClassDeclaration
            if (klass == null) {
                env.logger.warn("@JfxComponentBuilder can only be applied to classes.", symbol)
                continue
            }

            val qName = klass.qualifiedName?.asString()
            if (qName == null) {
                env.logger.warn("@JfxComponentBuilder: class without qualifiedName is not supported.", klass)
                continue
            }

            if (qName in processed) continue

            val valid = klass.validate()
            if (debug) {
                env.logger.warn("@JfxComponentBuilder: processing $qName (valid=$valid)", klass)
            }

            val generated = generateBuilderFor(klass)
            when {
                generated -> processed += qName
                valid -> processed += qName
                else -> deferred += klass
            }
        }

        return deferred
    }

    private fun generateBuilderFor(klass: KSClassDeclaration): Boolean {
        val packageName = klass.packageName.asString()
        val className = klass.simpleName.asString()

        val annotation = klass.annotations.firstOrNull {
            it.annotationType.resolve().declaration.qualifiedName?.asString() == annotationFqName
        } ?: run {
            env.logger.error("@JfxComponentBuilder not found on $packageName.$className", klass)
            return false
        }

        val args = annotation.arguments.associate { it.name?.asString().orEmpty() to it.value }

        val builderNameFromAnn = (args["name"] as? String).orEmpty()
        val tagFromAnn = (args["tag"] as? String).orEmpty()
        val classesFromAnn = (args["classes"] as? List<*>)?.filterIsInstance<String>().orEmpty()
        val afterBuildMode = args["afterBuild"]?.toString()?.substringAfterLast('.') ?: "SCHEDULED"

        val builderName = builderNameFromAnn.ifBlank { className.lowercaseFirst() }

        val constructor = klass.primaryConstructor ?: klass.getConstructors().firstOrNull()
        if (constructor == null) {
            env.logger.error("@JfxComponentBuilder: $packageName.$className has no constructor.", klass)
            return false
        }

        val nodeParam = constructor.parameters.find { it.name?.asString() == "node" } ?: constructor.parameters.firstOrNull()
        if (nodeParam == null) {
            env.logger.error("@JfxComponentBuilder: $packageName.$className has no constructor parameters.", klass)
            return false
        }

        val nodeType = nodeParam.type.resolve()
        val nodeTypeRendered = renderType(nodeType)

        val tag = tagFromAnn.ifBlank {
            deriveTag(nodeType) ?: run {
                env.logger.error(
                    "@JfxComponentBuilder: cannot derive tag from node type '$nodeTypeRendered' for $packageName.$className. Specify tag explicitly.",
                    nodeParam
                )
                return false
            }
        }

        val builderParams = ArrayList<String>()
        val callArgs = ArrayList<String>(constructor.parameters.size)

        for (p in constructor.parameters) {
            if (p == nodeParam) {
                callArgs += "el"
                continue
            }

            val paramNameRaw = p.name?.asString()
            if (paramNameRaw.isNullOrBlank()) {
                env.logger.error("@JfxComponentBuilder: unnamed constructor parameter not supported for $packageName.$className.", p)
                return false
            }
            val paramName = escapeKotlinIdentifier(paramNameRaw)

            val injectedExpr = injectedArgExpr(p)
            if (injectedExpr != null) {
                callArgs += injectedExpr
                continue
            }

            val typeRendered = renderType(p.type.resolve())
            builderParams += "$paramName: $typeRendered"
            callArgs += paramName
        }

        val hasAfterBuild = klass.getAllFunctions().any { fn ->
            fn.simpleName.asString() == "afterBuild" && fn.parameters.isEmpty()
        }

        val shouldCallAfterBuild = hasAfterBuild && afterBuildMode != "NONE"

        val fileName = "${className}_JfxBuilder"

        val containingFile = klass.containingFile
        val deps = if (containingFile != null) Dependencies(aggregating = false, sources = arrayOf(containingFile)) else Dependencies.ALL_FILES

        val file = env.codeGenerator.createNewFile(
            dependencies = deps,
            packageName = packageName,
            fileName = fileName
        )

        OutputStreamWriter(file, Charsets.UTF_8).use { out ->
            out.appendLine("package $packageName")
            out.appendLine()
            out.appendLine("// Generated by jfx2-component-ksp. Do not edit by hand.")
            out.appendLine()
            out.appendLine("context(scope: jFx2.core.capabilities.NodeScope)")
            out.append("fun ${escapeKotlinIdentifier(builderName)}(")

            val params = ArrayList<String>(builderParams.size + 1)
            params.addAll(builderParams)
            params += "block: context(jFx2.core.capabilities.NodeScope) $className.() -> Unit = {}"

            if (params.isEmpty()) {
                out.append(")")
            } else {
                out.appendLine()
                for ((i, p) in params.withIndex()) {
                    val suffix = if (i == params.lastIndex) "" else ","
                    out.appendLine("    $p$suffix")
                }
                out.append(")")
            }

            out.appendLine(": $className =")
            out.appendLine("    jFx2.core.codegen.buildComponent(")
            out.appendLine("        tag = \"${escapeKotlinString(tag)}\",")
            if (classesFromAnn.isNotEmpty()) {
                val renderedClasses = classesFromAnn.joinToString(", ") { "\"${escapeKotlinString(it)}\"" }
                out.appendLine("        classes = arrayOf($renderedClasses),")
            }

            out.appendLine("        create = { el: $nodeTypeRendered -> $className(${callArgs.joinToString(", ")}) },")
            out.appendLine("        block = block,")

            if (shouldCallAfterBuild) {
                out.appendLine("        afterBuild = jFx2.core.codegen.AfterBuildMode.$afterBuildMode,")
                out.appendLine("        afterBuildCall = { afterBuild() },")
            }

            out.appendLine("    )")
        }

        return true
    }

    private fun injectedArgExpr(p: KSValueParameter): String? {
        val type = p.type.resolve()
        val qName = type.declaration.qualifiedName?.asString() ?: return null
        return when (qName) {
            "jFx2.core.capabilities.UiScope" -> "scope.ui"
            "jFx2.core.capabilities.NodeScope" -> "scope"
            "jFx2.core.Ctx" -> "scope.ctx"
            "jFx2.core.capabilities.DisposeScope" -> "scope.dispose"
            "jFx2.core.dom.DomInsertPoint" -> "scope.insertPoint"
            "jFx2.core.capabilities.DomScope" -> "scope.ui.dom"
            "jFx2.core.capabilities.BuildScope" -> "scope.ui.build"
            else -> null
        }
    }

    private fun deriveTag(nodeType: KSType): String? {
        val qName = nodeType.declaration.qualifiedName?.asString() ?: return null
        return when (qName) {
            "org.w3c.dom.HTMLDivElement" -> "div"
            "org.w3c.dom.HTMLSpanElement" -> "span"
            "org.w3c.dom.HTMLButtonElement" -> "button"
            "org.w3c.dom.HTMLImageElement" -> "img"
            "org.w3c.dom.HTMLAnchorElement" -> "a"
            "org.w3c.dom.HTMLInputElement" -> "input"
            "org.w3c.dom.HTMLFormElement" -> "form"
            "org.w3c.dom.HTMLTextAreaElement" -> "textarea"
            "org.w3c.dom.HTMLSelectElement" -> "select"
            "org.w3c.dom.HTMLOptionElement" -> "option"
            "org.w3c.dom.HTMLHRElement" -> "hr"
            else -> null
        }
    }

    private fun renderType(type: KSType): String {
        val decl = type.declaration
        val base = decl.qualifiedName?.asString() ?: decl.simpleName.asString()

        val args = type.arguments
        val withArgs = if (args.isEmpty()) base else {
            base + args.joinToString(prefix = "<", postfix = ">") { renderTypeArg(it) }
        }

        return withArgs + if (type.nullability == Nullability.NULLABLE) "?" else ""
    }

    private fun renderTypeArg(arg: KSTypeArgument): String {
        if (arg.variance == Variance.STAR) return "*"
        val t = arg.type?.resolve() ?: return "*"
        return when (arg.variance) {
            Variance.COVARIANT -> "out ${renderType(t)}"
            Variance.CONTRAVARIANT -> "in ${renderType(t)}"
            Variance.INVARIANT -> renderType(t)
            Variance.STAR -> "*"
        }
    }

    private fun String.lowercaseFirst(): String =
        replaceFirstChar { c -> c.lowercase() }

    private fun escapeKotlinIdentifier(name: String): String {
        if (name.isEmpty()) return name
        return if (name in kotlinKeywords) "`$name`" else name
    }

    private fun escapeKotlinString(value: String): String =
        buildString(value.length + 8) {
            for (ch in value) {
                when (ch) {
                    '\\' -> append("\\\\")
                    '\"' -> append("\\\"")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")
                    else -> append(ch)
                }
            }
        }

    private val kotlinKeywords = setOf(
        "package", "as", "typealias", "class", "this", "super",
        "val", "var", "fun", "for", "null", "true", "false",
        "is", "in", "throw", "return", "break", "continue",
        "object", "if", "try", "else", "while", "do", "when",
        "interface", "typeof"
    )
}
