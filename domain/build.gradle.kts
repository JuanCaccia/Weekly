plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.example.weekly.domain"
    compileSdk = 35

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }
}

dependencies {
    // Para el contrato reactivo
    implementation("androidx.lifecycle:lifecycle-livedata-core:2.6.1")
    
    // Inyección de dependencias
    implementation("javax.inject:javax.inject:1")
    
    // Testing
    testImplementation(libs.junit)

    coreLibraryDesugaring(libs.desugar.jdk.libs)
}
