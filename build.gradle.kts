import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.3.20-RC" apply false
    kotlin("plugin.allopen") version "2.3.20-RC" apply false
    kotlin("plugin.noarg") version "2.3.20-RC" apply false
    kotlin("plugin.jpa") version "2.3.20-RC" apply false
}

subprojects {

    plugins.withId("java") {
        // optional: toolchain / sourceCompatibility
        extensions.configure<JavaPluginExtension> {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(21)) // oder 17/25
            }
        }
    }

    // Kotlin Compiler / Target etc.
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21) // oder 17/25 etc.
            freeCompilerArgs.addAll(
                "-Xjsr305=strict", "-Xannotation-default-target=param-property"
            )
        }
    }

    // ---- all-open Konfiguration ----
    plugins.withId("org.jetbrains.kotlin.plugin.allopen") {
        extensions.configure<org.jetbrains.kotlin.allopen.gradle.AllOpenExtension>("allOpen") {
            annotation("org.springframework.stereotype.Component")
            annotation("org.springframework.transaction.annotation.Transactional")
            annotation("org.springframework.scheduling.annotation.Async")

            annotation("jakarta.persistence.Entity")
            annotation("jakarta.persistence.MappedSuperclass")
            annotation("jakarta.persistence.Embeddable")
        }
    }

    // ---- no-arg Konfiguration ----
    plugins.withId("org.jetbrains.kotlin.plugin.noarg") {
        extensions.configure<org.jetbrains.kotlin.noarg.gradle.NoArgExtension>("noArg") {
            annotation("jakarta.persistence.Entity")
            annotation("jakarta.persistence.MappedSuperclass")
            annotation("jakarta.persistence.Embeddable")

            // entspricht: <option>noarg:com.anjunar.technologyspeaks.rest.NoArg</option>
            annotation("com.anjunar.technologyspeaks.rest.NoArg")
        }
    }
}
