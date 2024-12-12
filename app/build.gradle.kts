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

import com.teixeira0x.subtypo.build.BuildConfig
import java.util.Base64

plugins {
  id("com.android.application")
  id("kotlin-android")
  id("kotlin-parcelize")
  id("kotlin-kapt")

  id("com.google.dagger.hilt.android")
  id("com.mikepenz.aboutlibraries.plugin")
}

android {
  namespace = BuildConfig.packageName

  defaultConfig {
    applicationId = BuildConfig.packageName
    vectorDrawables.useSupportLibrary = true
  }

  dependenciesInfo {
    // Disables dependency metadata when building APKs.
    includeInApk = false
    // Disables dependency metadata when building Android App Bundles.
    includeInBundle = false
  }

  signingConfigs {
    val encodedKey = System.getenv("SIGNING_KEY_BASE64")
    if (encodedKey != null) {
      create("release") {
        val signingKeyFile =
          file("./signing-key.jks").apply {
            writeBytes(Base64.getDecoder().decode(encodedKey))
          }

        storeFile = signingKeyFile
        storePassword =
          System.getenv("SIGNING_KEY_PASSWORD")
            ?: throw GradleException("KEY_PASSWORD not set")
        keyAlias =
          System.getenv("SIGNING_KEY_ALIAS")
            ?: throw GradleException("KEY_ALIAS not set")
        keyPassword =
          System.getenv("SIGNING_KEY_PASSWORD")
            ?: throw GradleException("KEY_PASSWORD not set")
      }
    }
  }

  buildTypes {
    debug { isDebuggable = true }

    release {
      signingConfig = signingConfigs.findByName("release")
      isMinifyEnabled = true
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro",
      )
    }
  }

  compileOptions { isCoreLibraryDesugaringEnabled = true }

  kapt {
    correctErrorTypes = true
    arguments { arg("room.schemaLocation", "$projectDir/schemas") }
  }

  buildFeatures {
    viewBinding = true
    dataBinding = true
    buildConfig = true
  }

  aboutLibraries {
    // Remove the "generated" timestamp to allow for reproducible builds
    excludeFields = arrayOf("generated")
  }
}

dependencies {
  coreLibraryDesugaring(libs.android.desugar)
  implementation(libs.androidx.appcompat)
  implementation(libs.androidx.annotation)
  implementation(libs.androidx.core)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.fragment.ktx)
  implementation(libs.androidx.lifecycle.runtime)
  implementation(libs.androidx.lifecycle.viewmodel)
  implementation(libs.androidx.preference)
  implementation(libs.androidx.exoplayer)
  implementation(libs.androidx.exoplayer.dash)
  implementation(libs.androidx.exoplayer.ui)

  implementation(libs.google.material)
  implementation(libs.google.gson)

  implementation(libs.common.utilcode)
  implementation(libs.common.android.coroutines)

  val roomVersion = "2.6.1"
  kapt("androidx.room:room-compiler:$roomVersion")
  implementation("androidx.room:room-runtime:$roomVersion")
  implementation("androidx.room:room-ktx:$roomVersion")

  val hiltVersion = "2.53.1"
  kapt("com.google.dagger:hilt-compiler:$hiltVersion")
  implementation("com.google.dagger:hilt-android:$hiltVersion")

  implementation("com.mikepenz:aboutlibraries:11.2.3")
  implementation("com.mikepenz:aboutlibraries-core:11.2.3")

  debugImplementation(libs.common.leakcanary)
}
