plugins {
    kotlin("multiplatform")
}

kotlin {
    js(IR) {
        browser()
    }

    sourceSets {
        val jsMain by getting {
            dependencies {
                // wichtig: Kotlin/JS standard stuff
                implementation(kotlin("stdlib"))
            }
        }
    }
}