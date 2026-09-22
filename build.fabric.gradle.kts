import org.gradle.jvm.tasks.Jar
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.compile.JavaCompile

plugins {
    id("net.fabricmc.fabric-loom-remap")
}

version = "${property("mod.version")}+${sc.current.version}"
group = property("maven.group") as String

base {
    archivesName = property("archives.base.name") as String
}

repositories {
    maven("https://maven.isxander.dev/releases")
    maven("https://maven.terraformersmc.com")
    maven("https://maven.gegy.dev")
}

loom {
    splitEnvironmentSourceSets()

    mods {
        create("waylight") {
            sourceSet(sourceSets["main"])
            sourceSet(sourceSets["client"])
        }
    }

    runConfigs.all {
        ideConfigGenerated(true)
        runDir = "../../run"
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${sc.current.version}")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")

    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric")}")

    modImplementation("dev.isxander:yet-another-config-lib:${property("deps.yacl")}") {
        exclude(group = "net.fabricmc.fabric-api", module = "fabric-api")
    }
    include("dev.isxander:yet-another-config-lib:${property("deps.yacl")}")

    modCompileOnly("com.terraformersmc:modmenu:${property("deps.modmenu")}")
    modRuntimeOnly("com.terraformersmc:modmenu:${property("deps.modmenu")}")

    modImplementation("dev.lambdaurora.lambdynamiclights:lambdynamiclights-runtime:${property("deps.lambdynamiclights")}")
}

tasks.named("check") {
    dependsOn(":formatting:spotlessCheck")
}

tasks.processResources {
    val props = mapOf(
        "version" to project.version,
        "target_minecraft" to project.property("mod.target"),
        "target_java" to project.property("target.java"),
        "target_fabricloader" to project.property("deps.fabric_loader"),
        "target_yacl" to project.property("target.yacl"),
        "target_lambdynamiclights" to project.property("target.lambdynamiclights")
    )

    inputs.properties(props)

    filesMatching("fabric.mod.json") {
        expand(props)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.named<Jar>("jar") {
    from("LICENSE") {
        rename { "${it}_${property("archives.base.name")}" }
    }
}

tasks.register<Copy>("buildAndCollect") {
    group = "build"
    from(tasks.named("remapJar").map { (it as Jar).archiveFile })
    into(rootProject.layout.buildDirectory.dir("libs/fabric"))
    dependsOn("build")
}
