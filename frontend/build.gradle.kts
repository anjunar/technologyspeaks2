import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "2.3.0"
}

repositories {
    mavenCentral()
}

kotlin {
    js(IR) {
        binaries.executable()

        browser {
            commonWebpackConfig {
                cssSupport { enabled.set(true) }
                devServer?.apply {
                    port = 4200

                    proxy = mutableListOf(
                        KotlinWebpackConfig.DevServer.Proxy(
                            context = mutableListOf("/service"),
                            target = "http://localhost:8080",
                            changeOrigin = true,
                            secure = false
                        )
                    )
                }            }
        }
        compilerOptions {
            target = "es2015"
        }
    }

    sourceSets {
        val jsMain by getting {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-serialization-json-js:1.9.0")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
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
