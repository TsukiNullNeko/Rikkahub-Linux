plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.multiplatform)
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
        freeCompilerArgs.addAll(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3ExpressiveApi",
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
            "-opt-in=androidx.compose.foundation.layout.ExperimentalLayoutApi",
            "-opt-in=androidx.compose.animation.ExperimentalAnimationApi",
            "-opt-in=androidx.compose.ui.ExperimentalComposeUiApi",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
            "-opt-in=kotlin.ExperimentalStdlibApi",
            "-opt-in=kotlin.uuid.ExperimentalUuidApi",
            "-Xjvm-default=all"
        )
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    implementation(libs.org.json)
    api(compose.runtime)
    api(compose.foundation)
    api(compose.material3)
    api(compose.ui)
    api(compose.uiUtil)
    api(compose.animation)
    api(libs.kotlinx.coroutines.core)
    api(libs.lifecycle.viewmodel)
    api(libs.lifecycle.viewmodel.compose)
    api(libs.lifecycle.runtime.compose)
    api(libs.koin.core)
    api(libs.koin.compose.viewmodel)
    api(libs.kotlinx.serialization.json)
    api(libs.kotlinx.datetime)
    api(libs.slf4j.api)
    api(libs.metadata.extractor)
    api(libs.xmlpull)
    api(libs.zxing.core)
    api(libs.zxing.javase)
    api(libs.kxml2)
    implementation(libs.slf4j.simple)
}