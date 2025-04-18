plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.protobuf)
    alias(libs.plugins.gradle.test.logger)
}

android {
    namespace = "com.justvinny.github.noadsepubreader"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.justvinny.github.noadsepubreader"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.2"

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
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    // Main Dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Desugaring
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // Other Dependencies
    implementation(libs.readium.streamer)
    implementation(libs.readium.adapter.pdfium)
    implementation(libs.epub4j.core) {
        exclude("xmlpull")
    }

    // DataStore
    implementation(libs.protobuf.javalite)
    implementation(libs.androidx.datastore)

    // Material Icon Extended
    implementation(libs.androidx.material.icons.extended)

    // Unit Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.junitparams)

    // Instrumentation Testing
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.mockk.android)
}

protobuf {
    protoc {
        val dependency = libs.protobuf.protoc.get()
        val protobuf =
            "${dependency.module.group}:${dependency.module.name}:${dependency.versionConstraint.requiredVersion}"
        val suffix = if (osdetector.os == "osx") ":osx-x86_64" else ""
        artifact = protobuf + suffix
    }

    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                create("java") {
                    option("lite")
                }
            }
        }
    }
}