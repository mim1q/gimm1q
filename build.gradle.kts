plugins {
    id("fabric-loom") version Versions.LOOM
    id("maven-publish")
    id("io.github.p03w.machete") version "2.0.1"
}

version = ModData.VERSION
group = ModData.GROUP

repositories {
}

dependencies {
    minecraft("com.mojang:minecraft:${Versions.MINECRAFT}")
    mappings("net.fabricmc:yarn:${Versions.YARN}:v2")
    modImplementation("net.fabricmc:fabric-loader:${Versions.FABRIC_LOADER}")

    modImplementation("net.fabricmc.fabric-api:fabric-api:${Versions.FABRIC_API}")

    afterEvaluate {
        "testmodImplementation"(sourceSets.main.get().output)
    }

    include(implementation("net.objecthunter:exp4j:0.4.8")!!)
}

java {
    withSourcesJar()
    withJavadocJar()

    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks {
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }

    withType<GenerateModuleMetadata> {
        dependsOn("optimizeOutputsOfRemapJar")
    }

    javadoc {
        // Disable no comment warning
        (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
    }

    jar {
        from("LICENSE") {
            rename { "${it}_${ModData.GROUP}" }
        }
    }

    processResources {
        inputs.property("version", ModData.VERSION)
        inputs.property("minecraft_version", Versions.MINECRAFT)
        inputs.property("loader_version", Versions.FABRIC_LOADER)
        filteringCharset = "UTF-8"

        filesMatching("fabric.mod.json") {
            expand(
                "version" to ModData.VERSION,
                "minecraft_version" to Versions.MINECRAFT,
                "loader_version" to Versions.FABRIC_LOADER
            )
        }
    }

    // Publish to GitHub Releases
    register<Exec>("publishToGithub") {
        group = "publishing"
        description = "Publishes the current version to GitHub Releases"
        workingDir = projectDir
        dependsOn("build")

        val jarName = "${ModData.ID}-${ModData.VERSION}"
        val changelog = with(file("changelogs/${ModData.VERSION}.md")) {
            if (exists()) readText() else ""
        }

        commandLine(
            "gh", "release",
            "create", ModData.VERSION,
            "build/libs/${jarName}.jar",
            "build/libs/${jarName}-javadoc.jar",
            "build/libs/${jarName}-sources.jar",
            "-t", "Gimm1q ${ModData.VERSION_TYPE} ${ModData.VERSION}",
            "-n", changelog,
        )
    }
}

sourceSets {
    create("testmod") {
        runtimeClasspath += main.get().runtimeClasspath
        compileClasspath += main.get().compileClasspath
        compileClasspath += sourceSets.getByName("testmod").compileClasspath
        runtimeClasspath += sourceSets.getByName("testmod").runtimeClasspath
    }
}

loom {
    runs {
        create("testmodClient") {
            client()
            ideConfigGenerated(project.rootProject == project)
            name("Testmod Client")
            source(sourceSets.getByName("testmod"))
        }
        create("testmodServer") {
            server()
            ideConfigGenerated(project.rootProject == project)
            name("Testmod Server")
            source(sourceSets.getByName("testmod"))
        }
    }
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = ModData.GROUP
            artifactId = ModData.ID
            version = ModData.VERSION
        }
    }

    repositories {
        maven {
            url = uri("https://repo.repsy.io/mvn/mim1q/mods/")
            credentials {
                username = properties["repsyUsername"] as String
                password = properties["repsyPassword"] as String
            }
        }
    }
}