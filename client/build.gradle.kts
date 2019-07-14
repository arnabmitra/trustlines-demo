import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

repositories {
    mavenCentral()
}

plugins {
    kotlin("jvm") version "1.2.71"
    kotlin("plugin.spring") version "1.2.71"
}

dependencies {
    implementation(project(":trustlines-model"))

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.7")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.9.7")
    implementation( "org.slf4j:slf4j-api:1.7.25")

    api("io.github.openfeign:feign-core:10.1.0")
    api("io.github.openfeign:feign-jackson:10.1.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test:2.1.6.RELEASE")
    testImplementation ("org.jetbrains.kotlin:kotlin-test")
    implementation("com.google.crypto.tink:tink:1.2.2")


}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}

task("execute", JavaExec::class) {
    main = "com.amitra.trustlines.client.ApplicationKt"
    classpath = sourceSets["main"].runtimeClasspath
}
