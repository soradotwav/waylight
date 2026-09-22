plugins {
    id("dev.kikugie.stonecutter")
    id("net.fabricmc.fabric-loom-remap") version "1.15.5" apply false
    id("net.fabricmc.fabric-loom") version "1.15.5" apply false
    id("com.diffplug.spotless") version "8.3.0" apply false
}

stonecutter active "1.21.11-fabric" /* [SC] DO NOT EDIT */

stonecutter parameters {
    val loader = node.metadata.project.substringAfterLast('-')
    constants["fabric"] = loader == "fabric"
    swaps["mod_version"] = "\"${property("mod.version")}\";"
}

subprojects {
    apply(plugin = "com.diffplug.spotless")

    repositories {
        mavenCentral()
    }

    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        java {
            target(fileTree("src") {
                include("**/*.java")
            })
            palantirJavaFormat()
            formatAnnotations()
            trimTrailingWhitespace()
            endWithNewline()
        }

        format("misc") {
            target("*.gradle.kts", "*.md", ".gitignore", ".gitattributes")
            trimTrailingWhitespace()
            endWithNewline()
        }
    }
}
