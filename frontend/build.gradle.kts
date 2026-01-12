plugins {
    kotlin("multiplatform") version "2.3.0"
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
            }
        }
        compilerOptions {
            target = "es2015"
        }
    }

    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
            }
        }
    }
}
