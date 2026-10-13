// RikkaHub-Linux root build file
plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.ksp) apply false
}
// Compose Multiplatform's plugin maps compose.material3 to the stable 1.9.0 line, which lacks
// the Material 3 Expressive APIs this app relies on. Force the expressive line explicitly.
subprojects {
    configurations.configureEach {
        resolutionStrategy {
            force("org.jetbrains.compose.material3:material3:1.13.0-alpha02")
        }
    }
}
