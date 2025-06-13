plugins {
    application
    java
}

repositories {
    mavenCentral()
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
}

val lwjglVersion = "3.3.1"
val os = org.gradle.internal.os.OperatingSystem.current()
val lwjglNatives = when {
    os.isWindows -> "natives-windows"
    os.isLinux -> "natives-linux"
    os.isMacOsX -> "natives-macos"
    else -> error("Unsupported OS")
}
dependencies {
    implementation("org.lwjgl:lwjgl:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-glfw:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-opengl:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-stb:$lwjglVersion")
    implementation("org.joml:joml:1.10.5")

    runtimeOnly("org.lwjgl:lwjgl:$lwjglVersion:$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-glfw:$lwjglVersion:$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-opengl:$lwjglVersion:$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-stb:$lwjglVersion:$lwjglNatives")
}

application {
    mainClass.set("org.example.Main")
}

fun osClassifier(os: org.gradle.internal.os.OperatingSystem): String = when {
    os.isWindows -> "windows"
    os.isLinux -> "linux"
    os.isMacOsX -> "macos"
    else -> error("Unknown OS")
}
