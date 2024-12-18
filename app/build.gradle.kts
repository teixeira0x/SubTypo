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
import com.teixeira0x.subtypo.build.SigningKeyUtils.getSigningKeyAlias
import com.teixeira0x.subtypo.build.SigningKeyUtils.getSigningKeyPass
import com.teixeira0x.subtypo.build.SigningKeyUtils.writeSigningKey
import com.teixeira0x.subtypo.build.signingKeyFile

plugins {
  id("com.android.application")
  id("kotlin-android")
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
    writeSigningKey()

    val alias: String? = getSigningKeyAlias()
    val pass: String? = getSigningKeyPass()

    if (alias != null && pass != null) {
      create("release") {
        storeFile = signingKeyFile.get().asFile
        storePassword = pass
        keyAlias = alias
        keyPassword = pass
      }
    }
  }

  buildTypes {
    debug { applicationIdSuffix = ".debug" }

    release {
      signingConfigs.findByName("release")?.also { signingConfig = it }

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
  kapt(libs.androidx.room.compiler)
  implementation(libs.androidx.room.runtime)
  implementation(libs.androidx.room.ktx)

  // Google
  implementation(libs.google.material)
  implementation(libs.google.gson)
  kapt(libs.google.hilt.compiler)
  implementation(libs.google.hilt)

  // Kotlin
  implementation(libs.kotlin.coroutines.android)

  // Utils
  implementation(libs.util.utilcode)
  implementation(libs.util.aboutlibraries)
  implementation(libs.util.aboutlibraries.core)
  debugImplementation(libs.util.leakcanary)

  // Test
  testImplementation(libs.tests.junit)
}
