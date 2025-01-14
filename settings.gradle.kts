plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
rootProject.name = "class-loader-test"
include("transformer-interface")
include("transformer-identity")
include("transformer-base64encode")
include("transformer-base64decode")
include("worker-null")
