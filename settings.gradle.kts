plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
rootProject.name = "marble-run-compositor"
include("transformer-interface")
include("transformer-identity")
include("transformer-base64encode")
include("transformer-base64decode")
include("workers-lib-std")
include("intellij-mbrun-plugin")
