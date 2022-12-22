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

    jacoco
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

    maven {
        url = uri("https://packages.confluent.io/maven/")
    }
}

apply(plugin = "org.jlleitschuh.gradle.ktlint")

ktlint {
    disabledRules.set(setOf("no-wildcard-imports"))
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    val coroutinesVersion = "1.6.4"
    val beamVersion = "2.42.0"

    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    implementation("org.apache.beam:beam-sdks-java-core:$beamVersion")
    runtimeOnly("org.apache.beam:beam-runners-google-cloud-dataflow-java:$beamVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")

    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // This dependency is used by the application.
    implementation("com.google.guava:guava:31.0.1-jre")

    implementation("org.apache.avro:avro:1.11.0")

    // Use the Kotlin test library.
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.apache.beam:beam-runners-direct-java:$beamVersion")
}

application {
    // Define the main class for the application.
    mainClass.set("wordle.solver.kt.AppKt")
}

tasks.test {
    useJUnitPlatform()

    finalizedBy(tasks.jacocoTestCoverageVerification)
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(false)
        csv.required.set(false)
        html.outputLocation.set(layout.buildDirectory.dir("jacocoHtml"))
    }
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.9".toBigDecimal()
            }
        }
    }
}

task(name = "generateAvgEliminated", type = JavaExec::class) {
    classpath = sourceSets["main"].runtimeClasspath
    main = "ai.deeppow.preprocessors.GenerateAverageEliminatedMap"
    jvmArgs = listOf("-XX:+HeapDumpOnOutOfMemoryError")
}

task(name = "CreateMostEliminatedPipeline", type = JavaExec::class) {
    classpath = sourceSets["main"].runtimeClasspath
    main = "ai.deeppow.pipelines.CreateMostEliminatedPipeline"
    args = listOf(
        "--runner=DataflowRunner",
        "--project=api-project-177134456185",
        "--region=us-central1",
        "--dataflowServiceOptions=enable_prime",
        "--templateLocation=gs://deeppow-dataflow-templates/templates/CreateMostEliminatedPipeline",
        "--tempLocation=gs://deeppow-dataflow-templates/temp/CreateMostEliminatedPipeline",
        "--gcpTempLocation=gs://deeppow-dataflow-templates/temp/CreateMostEliminatedPipeline",
        "--stagingLocation=gs://deeppow-dataflow-templates/staging/CreateMostEliminatedPipeline"
    )
}

task(name = "Wordle", type = JavaExec::class) {
    classpath = sourceSets["main"].runtimeClasspath
    main = "ai.deeppow.App"
    standardInput = System.`in`
}
