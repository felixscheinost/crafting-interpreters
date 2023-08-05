import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsExec
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension
import java.io.ByteArrayOutputStream

plugins {
  kotlin("multiplatform")
  application
  id("com.google.devtools.ksp")
  id("idea")
}

fun which(cmd: String): String {
  val stdout = ByteArrayOutputStream()
  exec {
    workingDir(rootProject.projectDir)
    // Note: This explicitly uses `nix develop . -c` so that it also works when invoking Gradle using IntelliJ
    commandLine("nix", "develop", ".", "-c", "which", cmd)
    standardOutput = stdout
    errorOutput = ByteArrayOutputStream()
  }
  return stdout.toString().trim()
}

val nodeCommandFromShellNix = which("node")
val yarnCommandFromShellNix = which("yarn")

rootProject.plugins.withType(NodeJsRootPlugin::class).whenObjectAdded {
  rootProject.extensions.getByType<NodeJsRootExtension>().apply {
    download = false
    nodeCommand = nodeCommandFromShellNix
  }
}

rootProject.plugins.withType(YarnPlugin::class).whenObjectAdded {
  rootProject.extensions.getByType<YarnRootExtension>().apply {
    download = false
    command = yarnCommandFromShellNix
  }
}

kotlin {
  jvm {
    // withJava: needed for the application/distribution plugin. otherwise just the "common" .jar is copied which is empty.
    //           the "meat" is in -jvm.jar
    withJava()
    compilations.all {
      kotlinOptions.jvmTarget = "17"
    }
    testRuns["test"].executionTask.configure {
      useJUnitPlatform()
    }
  }
  js(IR) {
    nodejs {
      testTask {
        useMocha()
      }
    }
    browser {
      testTask {
        useMocha()
      }
    }
    // This is default for LEGACY but required for the IR compiler
    binaries.executable()
  }
  @Suppress("UNUSED_VARIABLE")
  sourceSets {
    val commonMain by getting {
      kotlin {
        srcDir("build/generated/ksp/metadata/commonMain/kotlin")
      }
    }
    val commonTest by getting {
      dependencies {
        implementation(kotlin("test"))
      }
    }
    val jvmMain by getting
    val jvmTest by getting
    val jsMain by getting
    val jsTest by getting
  }
}

dependencies {
  add("kspCommonMainMetadata", project(":ksp-plugin"))
}

tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinCompile<*>>().all {
  if (name != "kspCommonMainKotlinMetadata") {
    dependsOn("kspCommonMainKotlinMetadata")
  }
}

application {
 mainClass.set("de.felixscheinost.klox.Lox")
}

val loxTestsChapter = "chap09_control"

val testLoxJvm by tasks.registering(Exec::class) {
  dependsOn(tasks.installDist)
  // Note: This explicitly uses `nix develop . -c` so that it also works when invoking Gradle using IntelliJ
  commandLine("nix", "develop", ".", "-c", "lox-test", loxTestsChapter, "--interpreter", buildDir.resolve("install/klox/bin/klox"))
}
tasks.check.get().dependsOn(testLoxJvm)

val testLoxJs by tasks.registering(Exec::class) {
  dependsOn("jsProductionExecutableCompileSync")
  val wrapperScript = buildDir.resolve("klox-js")
  doFirst {
    wrapperScript.writeText(
      """
        #! /usr/bin/env sh
        ${which("node")} --require ${rootProject.buildDir}/js/node_modules/source-map-support/register.js ${rootProject.buildDir}/js/packages/crafting-interpreters-klox/kotlin/crafting-interpreters-klox.js "$@"
      """.trimIndent()
    )
    wrapperScript.setExecutable(true)
  }
  // Note: This explicitly uses `nix develop . -c` so that it also works when invoking Gradle using IntelliJ
  commandLine("nix", "develop", ".", "-c", "lox-test", loxTestsChapter, "--interpreter", wrapperScript.absolutePath)
}
tasks.check.get().dependsOn(testLoxJs)

(tasks.findByPath("jsNodeRun") as NodeJsExec).apply {
  project.findProperty("args")?.let {
    args(it)
  }
}