import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

repositories {
	mavenCentral()
}

plugins {
	kotlin("jvm") version "1.2.71"
}


tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "1.8"
	}
}

dependencies {
	implementation(kotlin("stdlib-jdk8"))
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.7")
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.9.7")
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
	jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
	jvmTarget = "1.8"
}
