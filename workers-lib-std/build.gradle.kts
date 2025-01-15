plugins {
    kotlin("jvm")
}

group = "eu.ec.oib.training.alferio"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":transformer-interface"))
}