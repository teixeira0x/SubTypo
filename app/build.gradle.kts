plugins {
  id("com.android.application")
}

android {
  namespace = "com.teixeira.subtitles"
  compileSdk = 33

  defaultConfig {
    applicationId = "com.teixeira.subtitles"
    minSdk = 26
    targetSdk = 33
    versionCode = 1
    versionName = "1.0"

    vectorDrawables { useSupportLibrary = true }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }

  buildTypes {
    release {
      isMinifyEnabled = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }

  buildFeatures { viewBinding = true }
}

dependencies {
  implementation("androidx.appcompat:appcompat:1.6.1")
  implementation("androidx.constraintlayout:constraintlayout:2.1.4")
  implementation("com.google.android.material:material:1.9.0")
  implementation("com.google.code.gson:gson:2.10.1")
  implementation("com.blankj:utilcodex:1.31.1")
}
