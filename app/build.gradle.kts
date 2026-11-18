plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.jvm") version "2.4.21"

    id("org.jlleitschuh.gradle.ktlint") version "14.2.0"

//    id("com.bakdata.gradle.avro.gradle-avro-plugin") version "2.0.1"

    id("org.owasp.dependencycheck") version "13.0.0"

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
    version = "1.4.0"
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
}

application {
    // Define the main class for the application.
    mainClass.set("wordle.solver.kt.AppKt")
}

tasks.test {
    useJUnitPlatform()

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
                minimum = "0.5".toBigDecimal()
            }
        }
    }
}

tasks.register<JavaExec>("generateAvgEliminated") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("ai.deeppow.preprocessors.GenerateAverageEliminatedMap")
    jvmArgs = listOf("-XX:+HeapDumpOnOutOfMemoryError")
}

tasks.register<JavaExec>("CreateMostEliminatedPipeline") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("ai.deeppow.pipelines.CreateMostEliminatedPipeline")
    args =
        listOf(
            "--runner=DataflowRunner",
            "--project=api-project-177134456185",
            "--region=us-central1",
            "--dataflowServiceOptions=enable_prime",
            "--templateLocation=gs://deeppow-dataflow-templates/templates/CreateMostEliminatedPipeline",
            "--tempLocation=gs://deeppow-dataflow-templates/temp/CreateMostEliminatedPipeline",
            "--gcpTempLocation=gs://deeppow-dataflow-templates/temp/CreateMostEliminatedPipeline",
            "--stagingLocation=gs://deeppow-dataflow-templates/staging/CreateMostEliminatedPipeline",
        )
}

tasks.register<JavaExec>("Wordle") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("ai.deeppow.App")
    standardInput = System.`in`
}
