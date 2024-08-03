plugins {
  id("com.android.application")
  id("kotlin-android")
}

android {
  namespace = "com.teixeira.subtitles"
  compileSdk = 34

  defaultConfig {
    applicationId = "com.teixeira.subtitles"
    minSdk = 24
    targetSdk = 34
    versionCode = 1
    versionName = "1.1.0"

    vectorDrawables { useSupportLibrary = true }
  }

  signingConfigs {
    create("general") {
      storeFile = file("test.keystore")
      keyAlias = "test"
      keyPassword = "teixeira0x"
      storePassword = "teixeira0x"
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    isCoreLibraryDesugaringEnabled = true
  }

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
  implementation(libs.androidx.core)
  implementation(libs.androidx.preference)

  implementation(libs.google.material)
  implementation(libs.google.gson)

  implementation(libs.common.utilcode)
}