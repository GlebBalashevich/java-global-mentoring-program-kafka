import com.diffplug.gradle.spotless.SpotlessExtension

plugins {
    java
    checkstyle
    jacoco
    id("com.diffplug.spotless") version "6.7.2" apply false
    id("org.springframework.boot") version "2.7.3" apply false
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
    val junitVersion = "5.8.1"
    val lombokVersion = "1.18.24"

    dependencies {
        //Common
        annotationProcessor("org.projectlombok:lombok:$lombokVersion")
        compileOnly("org.projectlombok:lombok:$lombokVersion")
        annotationProcessor("org.mapstruct:mapstruct-processor:$mapstructVersion")
        implementation("org.mapstruct:mapstruct:$mapstructVersion")

        //Spring
        implementation("org.springframework.boot:spring-boot-starter-webflux")
        implementation("org.springframework.boot:spring-boot-starter-validation")

        //Test
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testImplementation("org.assertj:assertj-core:3.23.1")
        testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
        testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
        testImplementation("org.mockito:mockito-core:4.10.0")
    }

    configure<SpotlessExtension> {
        format("misc") {
            target("*.md", ".gitignore")
        }
        java {
            removeUnusedImports()
            importOrderFile("$rootDir/gradle/spotless/.importorder")
            targetExclude("*/generated/**/*.*")
            trimTrailingWhitespace()
            palantirJavaFormat()
        }
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

    tasks.compileJava{
        options.compilerArgs.plusAssign(listOf("-Xlint:unchecked", "-Xlint:deprecation"))
        dependsOn("spotlessApply")
    }

    tasks.test {
        finalizedBy(tasks.jacocoTestReport)
        useJUnitPlatform()
    }

}

