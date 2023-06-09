import com.diffplug.gradle.spotless.SpotlessExtension

plugins {
    java
    checkstyle
    jacoco
    id("com.diffplug.spotless") version "6.7.2" apply false
    id("org.springframework.boot") version "3.0.2" apply false
    id("io.spring.dependency-management") version "1.1.0" apply false
}

group = "com.epam.mentoring"
version = "1.0-SNAPSHOT"

subprojects {
    apply(plugin = "java")
    apply(plugin = "checkstyle")
    apply(plugin = "jacoco")
    apply(plugin = "com.diffplug.spotless")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")

    repositories {
        mavenCentral()
    }

    val mapstructVersion = "1.5.3.Final"
    val lombokVersion = "1.18.24"
    val testContainersVersion = "1.17.6"

    dependencies {
        //Common
        annotationProcessor("org.projectlombok:lombok:$lombokVersion")
        compileOnly("org.projectlombok:lombok:$lombokVersion")
        annotationProcessor("org.mapstruct:mapstruct-processor:$mapstructVersion")
        implementation("org.mapstruct:mapstruct:$mapstructVersion")

        //Spring
        implementation("org.springframework.boot:spring-boot-starter-webflux")
        implementation("org.springframework.boot:spring-boot-starter-validation")

        //Spring Cloud
        implementation("org.springframework.cloud:spring-cloud-stream-binder-kafka:4.0.1")

        //Test
        testImplementation("org.testcontainers:kafka:$testContainersVersion")
        testImplementation("org.testcontainers:mongodb:$testContainersVersion")
        testImplementation("org.testcontainers:junit-jupiter:$testContainersVersion")
        testImplementation("org.testcontainers:testcontainers:$testContainersVersion")
        testImplementation("io.projectreactor:reactor-test:3.2.3.RELEASE")
        testImplementation("org.springframework.boot:spring-boot-starter-test")
    }

    configure<CheckstyleExtension> {
        configFile = file("$rootDir/gradle/checkstyle/checkstyle.xml")
        configDirectory.set(file("$rootDir/gradle/checkstyle"))
        toolVersion = "10.3"
    }

    jacoco {
        toolVersion = "0.8.8"
    }

    tasks.build{
        dependsOn("jacocoTestCoverageVerification")
    }

    tasks.jacocoTestReport {
        reports {
            xml.required.set(false)
            csv.required.set(false)
            html.outputLocation.set(layout.buildDirectory.dir("jacocoHtml"))
        }
    }

    tasks.jacocoTestCoverageVerification {
        dependsOn("test")
        violationRules {
            rule {
                limit {
                    minimum = "0.8".toBigDecimal()
                }
            }
        }
    }

    configure<SpotlessExtension> {
        format("misc") {
            target("*.md", ".gitignore")
            commonFormat()
        }
        java {
            commonFormat()
            removeUnusedImports()
            importOrderFile("$rootDir/gradle/spotless/.importorder")
            eclipse().configFile("$rootDir/gradle/spotless/formatter.xml")
            targetExclude("*/generated/**/*.*")
        }
    }

    tasks.compileJava{
        options.compilerArgs.plusAssign(listOf("-Xlint:unchecked", "-Xlint:deprecation"))
        dependsOn("spotlessApply")
    }

    tasks.test {
        finalizedBy(tasks.jacocoTestReport)
        useJUnitPlatform()
    }

}

fun com.diffplug.gradle.spotless.FormatExtension.commonFormat() {
    trimTrailingWhitespace()
    indentWithSpaces()
    endWithNewline()
}

