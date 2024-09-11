plugins {
  id("com.android.application")
  id("kotlin-android")
  id("kotlin-parcelize")
}

android {
  namespace = "com.teixeira.subtitles"

  defaultConfig {
    applicationId = "com.teixeira.subtitles"

    vectorDrawables { useSupportLibrary = true }
  }

  dependenciesInfo {
    // Disables dependency metadata when building APKs.
    includeInApk = false
    // Disables dependency metadata when building Android App Bundles.
    includeInBundle = false
  }

  signingConfigs {
    create("general") {
      storeFile = file("test.keystore")
      keyAlias = "test"
      keyPassword = "teixeira0x"
      storePassword = "teixeira0x"
    }
  }

  compileOptions { isCoreLibraryDesugaringEnabled = true }

  buildTypes {
    release {
      isMinifyEnabled = false
      signingConfig = signingConfigs.getByName("general")
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
    debug {
      isMinifyEnabled = false
      signingConfig = signingConfigs.getByName("general")
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }

  buildFeatures {
    viewBinding = true
    buildConfig = true
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

  implementation(libs.common.utilcode)
  implementation(libs.common.android.coroutines)

  implementation(project(":subtitle"))
}
