import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.jpa")
    kotlin("plugin.allopen")
    kotlin("plugin.noarg")
}
group = "com.anjunar"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    api(project(":kotlin-universe"))
    api("com.google.guava:guava:33.5.0-jre")
    api("jakarta.json.bind:jakarta.json.bind-api:3.0.0")
    api("jakarta.persistence:jakarta.persistence-api:3.2.0")
    api("tools.jackson.core:jackson-databind:3.0.3")
    api("tools.jackson.module:jackson-module-kotlin:3.0.3")


}

tasks.test {
    useJUnitPlatform()
}
