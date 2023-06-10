import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension
import java.io.ByteArrayOutputStream

plugins {
  kotlin("multiplatform")
  application
}

fun which(cmd: String): String {
  val stdout = ByteArrayOutputStream()
  exec {
    workingDir(rootProject.projectDir)
    commandLine("direnv", "exec", ".", "which", cmd)
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
    compilations.all {
      kotlinOptions.jvmTarget = "17"
    }
    testRuns["test"].executionTask.configure {
      useJUnitPlatform()
    }
  }
  js(IR) {
    browser {
      testTask {
        useMocha()
      }
    }
    // This is default for LEGACY but required for the IR compiler
    binaries.executable()
  }
  sourceSets {
    val commonMain by getting
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

application {
  mainClass.set("de.felixscheinost.klox.Lox")
}