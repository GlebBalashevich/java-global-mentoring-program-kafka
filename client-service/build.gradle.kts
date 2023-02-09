group = "com.epam.mentoring.client"
version = "1.0-SNAPSHOT"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
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
