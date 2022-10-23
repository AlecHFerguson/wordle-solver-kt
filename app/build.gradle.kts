/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Kotlin application project to get you started.
 * For more details take a look at the 'Building Java & JVM projects' chapter in the Gradle
 * User Manual available at https://docs.gradle.org/7.5.1/userguide/building_java_projects.html
 */

plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.jvm") version "1.7.20"

    id("org.jlleitschuh.gradle.ktlint") version "10.3.0"

    id("com.github.davidmc24.gradle.plugin.avro") version "1.5.0"

    // Apply the application plugin to add support for building a CLI application in Java.
    application
}

buildscript {
    repositories {
        maven("https://plugins.gradle.org/m2/")
    }
    dependencies {
        classpath("org.jlleitschuh.gradle:ktlint-gradle:<current_version>")
    }
}

repositories {
    // Required to download KtLint
    mavenCentral()
}

apply(plugin = "org.jlleitschuh.gradle.ktlint")

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // This dependency is used by the application.
    implementation("com.google.guava:guava:31.0.1-jre")

    implementation("org.apache.avro:avro:1.11.0")

    // Use the Kotlin test library.
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

application {
    // Define the main class for the application.
    mainClass.set("wordle.solver.kt.AppKt")
}

tasks.test {
    useJUnitPlatform()
}
