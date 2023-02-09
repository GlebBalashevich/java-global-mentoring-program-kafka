group = "com.epam.mentoring.courier"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(project(":error-handling-shd"))
    implementation(project(":api-shd"))
}

tasks {
    bootJar {
        enabled = true
    }
    jar {
        enabled = false
    }
}


