plugins {
  kotlin("jvm") version "2.0.20" apply false
  kotlin("multiplatform") version "2.0.20" apply false
  id("com.google.devtools.ksp") version "2.0.20-1.0.25" apply false
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