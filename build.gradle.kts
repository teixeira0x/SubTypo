/*
 * This file is part of SubTypo.
 *
 * SubTypo is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * SubTypo is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with SubTypo.
 * If not, see <https://www.gnu.org/licenses/>.
 */

import com.android.build.gradle.BaseExtension
import com.teixeira0x.subtypo.build.BuildConfig
import com.teixeira0x.subtypo.build.VersionUtils
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Top-level build file where you can add configuration options common to all
// sub-projects/modules.
plugins {
  id("build-logic.root-project")
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.kotlin.android) apply false
  id("com.google.dagger.hilt.android") version "2.53.1" apply false
  id("com.mikepenz.aboutlibraries.plugin") version "11.2.3" apply false
}

fun Project.configureBaseExtension() {
  extensions.findByType(BaseExtension::class)?.run {
    compileSdkVersion(BuildConfig.compileSdk)
    buildToolsVersion = BuildConfig.buildTools

    defaultConfig {
      minSdk = BuildConfig.minSdk
      targetSdk = BuildConfig.targetSdk
      versionCode = VersionUtils.versionCode
      versionName = VersionUtils.versionName
    }

    compileOptions {
      sourceCompatibility = JavaVersion.VERSION_17
      targetCompatibility = JavaVersion.VERSION_17
    }
  }
}

subprojects {
  plugins.withId("com.android.application") { configureBaseExtension() }
  plugins.withId("com.android.library") { configureBaseExtension() }

  tasks.withType<KotlinCompile>().configureEach {
    compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
  }
}

tasks.register<Delete>("clean") { delete(rootProject.layout.buildDirectory) }
