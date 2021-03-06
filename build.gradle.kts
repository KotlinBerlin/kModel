@file:Suppress("SpellCheckingInspection")

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

plugins {
    kotlin("multiplatform") version "1.4.0" apply false
}

allprojects {
    group = "de.kotlin-berlin"
    version = "1.1-FINAL"

    repositories {
        jcenter()
        mavenLocal()
        maven {
            url = uri("https://dl.bintray.com/kotlin/kotlin-eap")
        }
    }
}

buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.8")
    }
}

tasks {
    task("buildAll") {
        group = "kotlinBerlin"
        doFirst {
            val tempBuildOrder = project.findProperty("de.kotlinBerlin.${project.name}.buildOrder") as String?
            tempBuildOrder?.split(",")?.forEach { tempProjectNames ->
                runBlocking {
                    for (it in tempProjectNames.split("#")) {
                        launch(Dispatchers.Default) {
                            println("Building ${project.name}-$it")
                            exec {
                                executable = "gradle.bat"
                                workingDir = File(projectDir, "${project.name}-$it")
                                args = listOf("build", "publishToMavenLocal")
                                standardOutput = java.io.ByteArrayOutputStream()
                            }
                        }
                    }
                }
            }
        }
    }
}