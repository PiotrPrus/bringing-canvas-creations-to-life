import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(libs.compose.material3)
    implementation(libs.composemediaplayer)
    implementation(libs.particle.emitter)
    implementation(libs.qrose)
    implementation(libs.graphics.shapes)
}

compose.desktop {
    application {
        mainClass = "dev.piotrprus.canvascreations.presentation.MainKt"

        nativeDistributions {
            packageName = "CanvasCreationsTalk"
            packageVersion = "1.0.0"
        }
    }
}

// Dev tool: render slides to PNGs without opening a window.
tasks.register<JavaExec>("renderSlides") {
    group = "presentation"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("dev.piotrprus.canvascreations.presentation.tools.RenderSlidesKt")
    args(
        (findProperty("out") as String?) ?: layout.buildDirectory.dir("slides").get().asFile.path,
        (findProperty("slides") as String?) ?: "",
        (findProperty("section") as String?) ?: "",
    )
}

// Dev tool: smoke-test the native video player in a small window.
tasks.register<JavaExec>("videoCheck") {
    group = "presentation"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("dev.piotrprus.canvascreations.presentation.tools.VideoCheckKt")
}
