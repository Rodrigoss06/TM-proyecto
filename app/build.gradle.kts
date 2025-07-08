plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")       // Plugin de Google Services para Firebase
}

android {
    namespace = "com.example.proyectfaseii"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.proyectfaseii"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    configurations.all {
        resolutionStrategy {
            force(libs.org.jetbrains.annotations)
            eachDependency {
                if (requested.group == "com.intellij" && requested.name == "annotations") {
                    useTarget("org.jetbrains:annotations:23.0.0")
                    because("Evitar conflicto de anotaciones duplicadas")
                }
            }
        }
    }

}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.tensorflow.lite)


    // — AÑADE ESTAS LÍNEAS —
    implementation(libs.androidx.viewpager2)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.fragment.ktx)
    // — FIN DE LAS LÍNEAS AÑADIDAS —

    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.androidx.lifecycle.common.jvm)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.firestore)
    implementation(libs.mlkit.text)
    implementation(libs.konfetti.xml)
    implementation(libs.androidx.room.compiler)
    implementation(libs.places)
    implementation(libs.androidx.media3.common.ktx)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.mpandroidchart)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
