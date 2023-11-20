plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
}

android {
    namespace = "com.example.firebaseecom"
    compileSdk = 34

    dataBinding{
        enable = true
    }

    defaultConfig {
        applicationId = "com.example.firebaseecom"
        minSdk = 24
        targetSdk = 33
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.gms:play-services-fido:20.1.0")
    implementation("com.google.firebase:firebase-auth:22.2.0")
    implementation("com.google.firebase:firebase-storage:20.3.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation ("androidx.viewpager2:viewpager2:1.0.0")//viewpager
    implementation ("com.github.bumptech.glide:glide:4.12.0")//glide
    kapt("com.github.bumptech.glide:compiler:4.12.0")
    implementation(platform("com.google.firebase:firebase-bom:32.3.1"))//firebase
    implementation ("com.google.android.material:material:1.2.0-alpha05")//NavMenu
    implementation("com.google.firebase:firebase-firestore:24.9.0")//cloudFirestore

    implementation ("com.google.dagger:hilt-android:2.44")//hilt
    kapt ("com.google.dagger:hilt-android-compiler:2.44")

    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.9")//coroutines

    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")//lifecycle
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-savedstate:2.6.2")
    kapt ("androidx.lifecycle:lifecycle-compiler:2.6.2")


    implementation("com.squareup.retrofit2:retrofit:2.9.0")//retrofit,okttp and moshi
    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.11.0"))
    implementation("com.squareup.retrofit2:converter-moshi:2.4.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.14.0")
    implementation("com.squareup.moshi:moshi-kotlin-codegen:1.14.0")
    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.14.0")



    val roomVersion = "2.6.0"
    implementation("androidx.room:room-runtime:$roomVersion")
    annotationProcessor("androidx.room:room-compiler:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")
    implementation ("androidx.room:room-ktx:2.6.0")
    kapt ("androidx.room:room-compiler:2.6.0")

    implementation("com.razorpay:checkout:1.6.33") //razorpay








}