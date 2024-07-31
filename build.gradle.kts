// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.kotlin) apply false
}

buildscript {
  dependencies {
    classpath("com.google.android.gms:oss-licenses-plugin:0.10.6")
  }
}

tasks.register<Delete>("clean") { delete(rootProject.buildDir) }
