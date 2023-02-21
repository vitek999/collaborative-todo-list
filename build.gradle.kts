import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.7.2" apply false
    id("io.spring.dependency-management") version "1.1.0" apply false

    kotlin("multiplatform") version "1.7.22"
    kotlin("plugin.serialization") version "1.7.22"

    kotlin("plugin.spring") version "1.7.22" apply false
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
    maven { url = uri("https://repo.spring.io/milestone") }
    maven { url = uri("https://repo.spring.io/snapshot") }
    maven { url = uri("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/kotlin-js-wrappers") }
}

kotlin {
    jvm("spring") {
        apply(plugin = "org.springframework.boot")
        apply(plugin = "io.spring.dependency-management")
        apply(plugin = "org.jetbrains.kotlin.plugin.spring")
        apply(plugin = "org.jetbrains.kotlin.plugin.serialization")

        tasks.withType<KotlinCompile> {
            kotlinOptions {
                freeCompilerArgs = listOf("-Xjsr305=strict")
                jvmTarget = "11"
            }
        }
        tasks.withType<Test> {
            useJUnitPlatform()
        }
    }
    js("react", IR) {
        binaries.executable()
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled = true
//                    enabled.set(true)   //1.8.0+
                }
                outputFileName = "main.js"
                outputPath = File(buildDir, "processedResources/spring/main/static")
            }
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("com.benasher44:uuid:0.6.0")
                implementation("org.jetbrains.kotlinx:kotlinx-html:0.8.0")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
            }
        }
        val commonTest by getting {
            dependsOn(commonMain)
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val commonClientMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation("io.rsocket.kotlin:rsocket-core:0.15.4")
                implementation("io.rsocket.kotlin:rsocket-transport-ktor-client:0.15.4")
            }
        }
        val reactMain by getting {
            dependsOn(commonMain)
            dependsOn(commonClientMain)
            dependencies {
                implementation("io.ktor:ktor-client-js:2.2.3")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react:18.2.0-pre.499")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom:18.2.0-pre.499")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-router-dom:6.8.1-pre.501")

                implementation(npm("todomvc-app-css", "2.0.0"))
                implementation(npm("todomvc-common", "1.0.0"))
            }
        }
        val springMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation("org.springframework.boot:spring-boot-starter-rsocket")
                implementation("org.springframework.boot:spring-boot-starter-webflux")

                implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
                implementation("org.jetbrains.kotlin:kotlin-reflect")
                implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
            }
        }
        val springTest by getting {
            dependencies {
                implementation("org.springframework.boot:spring-boot-starter-test")
                implementation("io.projectreactor:reactor-test")
            }
        }
    }
}

//tasks.getByName<Copy>("springProcessResources") {
//    dependsOn(tasks.getByName("reactBrowserDevelopmentWebpack"))
//}
