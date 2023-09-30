import org.jetbrains.kotlin.gradle.internal.Kapt3GradleSubplugin.Companion.findKaptConfiguration

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("io.realm.kotlin")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    // id("com.google.gms.google-services")
}

android {
    namespace = "com.alexlade.diaryapp"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.alexlade.diaryapp"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility =  JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.3.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation(platform("androidx.compose:compose-bom:2023.03.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // compose navigation
    implementation("androidx.navigation:navigation-compose:2.5.3")

    //firebase
    implementation("com.google.firebase:firebase-auth-ktx:21.1.0")
    implementation("com.google.firebase:firebase-storage-ktx:20.2.1")

    // room
    implementation("androidx.room:room-runtime:2.4.3")
    findKaptConfiguration("androidx.room:room-compiler:2.4.3")
    implementation("androidx.room:room-ktx:2.4.3")

    // runtime compose
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.2")

    // splash api
    implementation("androidx.core:core-splashscreen:1.0.0")

    //mongo db realm
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0-native-mt") {
        version { strictly("1.6.0-native-mt") }
    }
    implementation("io.realm.kotlin:library-sync:1.0.2")

    // dagger hilt
    implementation("com.google.dagger:hilt-android:2.44.2")
    kapt("com.google.dagger:hilt-compiler:2.44.2")
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")

    // google auth
    // implementation(:com.google.android.gms:play-services-auth:20.4.0")

    //coil
    implementation("io.coil-kt:coil-compose:2.2.2")

    // pager accompanist
    implementation("com.google.accompanist:accompanist-pager:0.27.0")

    // date-time picker
    implementation("io.github.vanpra.compose-material-dialogs:datetime:0.9.0")

    // message bar compose
    implementation("com.github.stevdza-san:MessageBarCompose:1.0.5")

    // one tap compose
    implementation("com.github.stevdza-san:OneTapCompose:1.0.0")

    // desugar jdk
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.2.0")



    implementation(platform("org.jetbrains.kotlin:kotlin-bom:1.8.0"))

}