plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.ksp)
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

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.generateKotlin", "true")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    implementation(project(":platform"))
    implementation(project(":hugeicons"))
    implementation(project(":common"))
    implementation(project(":ai"))
    implementation(project(":document"))
    implementation(project(":highlight"))
    implementation(project(":material3"))
    implementation(project(":search"))
    implementation(project(":speech"))
    implementation(project(":mediagen"))
    implementation(project(":oauth"))
    implementation(project(":workspace"))
    implementation(project(":ui"))

    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.material3)
    implementation(compose.ui)
    implementation(compose.components.resources)
    implementation(compose.desktop.currentOs)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)
    implementation(libs.okhttp)
    implementation(libs.okhttp.sse)
    implementation(libs.okhttp.logging)
    implementation(libs.retrofit)
    implementation(libs.retrofit.serialization.json)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.coil.compose)
    implementation(libs.coil.okhttp)
    implementation(libs.coil.svg)
    implementation(libs.koin.core)
    implementation(libs.koin.compose.multiplatform)
    implementation(libs.koin.compose.viewmodel)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.paging)
    implementation(libs.paging.common)
    implementation(libs.paging.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.quickjs)
    implementation(libs.jsoup)
    implementation(libs.zxing.core)
    implementation(libs.snakeyaml)
    implementation(libs.pebble)
    implementation(libs.diffutils)
    implementation(libs.commons.text)
    implementation(libs.metadata.extractor)
    implementation(libs.sqlite.jdbc)
    implementation(libs.sqlite.bundled)
    implementation(libs.jlatexmath.jvm)
    implementation(libs.pty4j)
    implementation(libs.slf4j.api)
    implementation(libs.slf4j.simple)
    implementation(libs.org.json)
    implementation(libs.modelcontextprotocol.kotlin.sdk)
    ksp(libs.androidx.room.compiler)
}