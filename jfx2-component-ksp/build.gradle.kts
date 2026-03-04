plugins {
    kotlin("jvm")
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.devtools.ksp:symbol-processing-api:2.3.6")
}

tasks.named<Jar>("jar") {
    // Kotlin 2.3.x + KSP: ensure Kotlin-compiled classes end up in the processor JAR.
    from(layout.buildDirectory.dir("classes/kotlin/main"))
}
