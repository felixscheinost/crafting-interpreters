plugins {
  kotlin("jvm") version "1.8.22" apply false
  kotlin("multiplatform") version "1.8.22" apply false
}

allprojects {
  group = "de.felixscheinost"
  version = "1.0-SNAPSHOT"

  repositories {
    mavenCentral()
  }
}