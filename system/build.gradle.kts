plugins {
    kotlin("jvm")
    kotlin("plugin.jpa")
    kotlin("plugin.allopen")
    kotlin("plugin.noarg")
}

group = "com.anjunar.technologyspeaks"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    api(project(":json-mapper"))
    api("org.springframework.boot:spring-boot-starter-web:4.0.1")
    api("org.springframework.boot:spring-boot-starter-mail:4.0.1")
    api("org.springframework.boot:spring-boot-starter-thymeleaf:4.0.1")
    api("org.springframework.boot:spring-boot-starter-data-jpa:4.0.1")
    api("org.thymeleaf:thymeleaf:3.1.3.RELEASE")
    api("org.hibernate.orm:hibernate-core:7.2.0.Final")
    api("org.hibernate.validator:hibernate-validator:9.1.0.Final")
    api("org.postgresql:postgresql:42.7.8")
    api("com.webauthn4j:webauthn4j-core:0.30.1.RELEASE")
    api("org.springframework.boot:spring-boot-starter-actuator:4.0.1")
    api("org.jetbrains.kotlin:kotlin-reflect:2.3.0")

}

tasks.test {
    useJUnitPlatform()
}