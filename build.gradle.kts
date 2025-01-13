plugins {
    kotlin("jvm") version "1.9.23"
    // Shadow plugin for creating the fat/uber JAR
    application
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "eu.ec.oib.training.alferio"
version = "1.0-SNAPSHOT"

// If you have an application entry point, configure it here:
application {
    // Replace with your actual main class (for Kotlin, typically "com.somepackage.MainKt")
    mainClass.set("eu.ec.oib.training.alferio.MainKt")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":transformer-interface"))
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}

// Configure the shadow JAR task
tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    // This removes the “-all” classifier so the artifact is just `myapp.jar`
    archiveClassifier.set("")

    // (Optional) Merge resource files if needed
    // mergeServiceFiles()
}


subprojects {
    // If you want all subprojects to use Kotlin/JVM, you could do:
    // Apply Kotlin JVM plugin automatically:
    apply(plugin = "org.jetbrains.kotlin.jvm")

    // Common settings for all subprojects can go here



// For convenience, let's name the jar file after the project
    tasks.named<Jar>("jar") {
        archiveBaseName.set(projectDir.name)
    }

// Create a copy task that depends on the JAR being built:
    tasks.register<Copy>("copyJarToTransformers") {
        dependsOn(tasks.named("jar"))      // Ensure the JAR is built first
        from(layout.buildDirectory.file("libs/${tasks.named<Jar>("jar").get().archiveFileName.get()}"))
        into(rootProject.layout.projectDirectory.dir("transformers"))
    }

// You can also make this copy run automatically when building the project:
    tasks.named("build") {
        dependsOn("copyJarToTransformers")
    }
}