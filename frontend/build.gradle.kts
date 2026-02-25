import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "2.3.20-RC"
    kotlin("plugin.js-plain-objects") version "2.3.20-RC"
}

repositories {
    mavenCentral()
}

kotlin {
    js(IR) {
        binaries.executable()

        nodejs()

        browser {
            commonWebpackConfig {
                cssSupport { enabled.set(true) }
                devServer?.apply {
                    port = 4200
                    open = true
                    proxy = mutableListOf(
                        KotlinWebpackConfig.DevServer.Proxy(
                            context = mutableListOf("/service"),
                            target = "http://localhost:8080",
                            changeOrigin = true,
                            secure = false
                        )
                    )
                }
            }
        }
        compilerOptions {
            target = "es2015"
        }
    }

    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-js:1.9.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")
                implementation(npm("@simplewebauthn/browser", "13.2.2"))
                implementation(npm("prosemirror-model", "1.25.4"))
                implementation(npm("prosemirror-state", "1.4.4"))
                implementation(npm("prosemirror-view", "1.41.5"))
                implementation(npm("prosemirror-transform", "1.11.0"))
                implementation(npm("prosemirror-commands", "1.7.1"))
                implementation(npm("prosemirror-history", "1.5.0"))
                implementation(npm("prosemirror-keymap", "1.2.3"))
                implementation(npm("prosemirror-schema-basic", "1.2.4"))
                implementation(npm("prosemirror-schema-list", "1.5.1"))
            }
        }
        all {
            languageSettings.enableLanguageFeature("ContextParameters")
        }

        compilerOptions {
            freeCompilerArgs.add("-Xcontext-parameters")
        }
    }
}
