plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.jvm") version "2.4.21"

    id("org.jlleitschuh.gradle.ktlint") version "14.2.0"

    id("com.fizzpod.osv-scanner") version "6.3.1"

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

ktlint {
    version = "1.4.0"
}

osvScanner {
    format = "json"
    failOn = "score"
    failOnThreshold = 7.0 // Fails on High (7.0–8.9) and Critical (9.0–10.0)
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

configurations {
    named("compileClasspath") {
        resolutionStrategy.activateDependencyLocking()
    }
}

dependencies {
    val coroutinesVersion = "1.6.4"
    val beamVersion = "2.77.0"

    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    implementation("org.apache.beam:beam-sdks-java-core:$beamVersion")
    implementation("org.apache.beam:beam-sdks-java-extensions-avro:$beamVersion")
    runtimeOnly("org.apache.beam:beam-runners-google-cloud-dataflow-java:$beamVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")

    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("com.google.guava:guava:33.8.0-jre")

    implementation("org.apache.avro:avro:1.12.2")

    testImplementation(kotlin("test"))

    constraints {
        implementation("com.fasterxml.jackson.core:jackson-core:2.22.4") {
            because("Fixes security vulnerability CVE-2026-89425")
        }
        implementation("org.xerial.snappy:snappy-java:1.1.10.12") {
            because("Fixes security vulnerability CVE-2026-93451")
        }
        implementation("io.opentelemetry:opentelemetry-api:1.68.0") {
            because("Fixes security vulnerability CVE-2026-118259")
        }
    }
}

application {
    // Define the main class for the application.
    mainClass.set("wordle.solver.kt.AppKt")
}

tasks.withType<JavaExec> {
    systemProperty("org.apache.avro.SERIALIZABLE_PACKAGES", "ai.deeppow.models")
    systemProperty("org.apache.avro.SERIALIZABLE_CLASSES", "java.util.LinkedHashMap")
}

tasks.withType<Test> {
    useJUnitPlatform()
    systemProperty("org.apache.avro.SERIALIZABLE_PACKAGES", "ai.deeppow.models")
    systemProperty("org.apache.avro.SERIALIZABLE_CLASSES", "java.util.LinkedHashMap")
    testLogging {
        events("passed", "skipped", "failed")
    }
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
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
    description = "Runs Wordle solver for all words. Generates JSON file showing results."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("ai.deeppow.preprocessors.GenerateAverageEliminatedMap")
    jvmArgs = listOf("-XX:+HeapDumpOnOutOfMemoryError")
}

tasks.register<JavaExec>("CreateMostEliminatedPipeline") {
    description = "Creates Google Cloud Dataflow pipeline which tests all words and finds best initial guess."
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
    description = "Play Wordle on command line"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("ai.deeppow.App")
    standardInput = System.`in`
}
