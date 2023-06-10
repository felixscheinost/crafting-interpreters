plugins {
  kotlin("jvm")
  application
}

dependencies {
  testImplementation(kotlin("test"))
}

tasks.test {
  useJUnitPlatform()
}

kotlin {
  jvmToolchain(17)
}

application {
  mainClass.set("de.felixscheinost.klox.Lox")
}