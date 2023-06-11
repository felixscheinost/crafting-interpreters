plugins {
  kotlin("jvm") version "1.8.22" apply false
  kotlin("multiplatform") version "1.8.22" apply false
  id("com.google.devtools.ksp") version "1.8.22-1.0.11" apply false
  id("idea")
}

allprojects {
  group = "de.felixscheinost"
  version = "1.0-SNAPSHOT"

  repositories {
    mavenCentral()
  }
}

idea {
  module {
    excludeDirs = excludeDirs + file(".direnv")
  }
}