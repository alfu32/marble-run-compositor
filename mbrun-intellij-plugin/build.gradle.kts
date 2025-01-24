plugins {
  id("java")
  id("org.jetbrains.kotlin.jvm") version "1.9.21"
  id("org.jetbrains.intellij") version "1.16.1"
}

group = "eu.ec.oib.training.alferio.mbrun.intellij.plugin"
version = "1.1"

repositories {
  mavenCentral()
}
dependencies{
  testImplementation("org.jetbrains.kotlin:kotlin-test")
  testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
  version.set("2023.2")
  type.set("IC") // Target IDE Platform

  plugins.set(listOf(
    "java",
    "org.intellij.intelliLang",
  ))
}

tasks {
  // Set the JVM compatibility versions
  withType<JavaCompile> {
    sourceCompatibility = "17"
    targetCompatibility = "17"
  }
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
  }


  patchPluginXml {
    version.set(project.version.toString())
    sinceBuild.set("231")  // e.g. 231 = 2023.1 baseline
    untilBuild.set("")     // or a specific upper build number
    changeNotes.set("""
            Initial version.
        """.trimIndent())
  }

  //// signPlugin {
  ////   certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
  ////   privateKey.set(System.getenv("PRIVATE_KEY"))
  ////   password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
  //// }
  ////
  //// publishPlugin {
  ////   token.set(System.getenv("PUBLISH_TOKEN"))
  //// }
}
