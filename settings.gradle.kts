pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.8.3"
}

stonecutter {
    create(rootProject) {
        version("1.21.11-fabric", "1.21.11").buildscript("build.fabric.gradle.kts")
        version("26.1-fabric", "26.1").buildscript("build.fabric-unobfuscated.gradle.kts")
        version("26.2-fabric", "26.2").buildscript("build.fabric-unobfuscated.gradle.kts")
        version("26.3-fabric", "26.3").buildscript("build.fabric-unobfuscated.gradle.kts")
        vcsVersion = "1.21.11-fabric"
    }
}

rootProject.name = "Waylight"
