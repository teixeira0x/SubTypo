plugins {
  id("com.android.application")
  id("kotlin-android")
  id("com.google.android.gms.oss-licenses-plugin")
}

android {
  namespace = "com.teixeira.subtitles"
  compileSdk = 34

  defaultConfig {
    applicationId = "com.teixeira.subtitles"
    minSdk = 24
    targetSdk = 34
    versionCode = 1
    versionName = "1.0"

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
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
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
  coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
  implementation("androidx.appcompat:appcompat:1.6.1")
  implementation("androidx.core:core:1.12.0")
  implementation("androidx.constraintlayout:constraintlayout:2.1.4")
  implementation("androidx.preference:preference:1.2.1")

  implementation("com.google.android.material:material:1.9.0")
  implementation("com.google.android.gms:play-services-oss-licenses:17.0.1")
  implementation("com.google.code.gson:gson:2.10.1")
  implementation("com.blankj:utilcodex:1.31.1")
}