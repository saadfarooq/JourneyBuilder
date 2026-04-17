plugins {
    base
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.jreleaser)
}

group = property("GROUP") as String
version = property("VERSION_NAME") as String

jreleaser {
    project {
        description.set("Type-safe step-by-step state machine for Android")
        copyright.set("2026 Saad Farooq")
    }
    signing {
        active.set(org.jreleaser.model.Active.NEVER)
    }
    deploy {
        maven {
            mavenCentral {
                create("sonatype") {
                    active.set(org.jreleaser.model.Active.ALWAYS)
                    url.set("https://central.sonatype.com/api/v1/publisher")
                    sign.set(false)
                    username.set(providers.gradleProperty("mavenCentralUsername").orNull)
                    password.set(providers.gradleProperty("mavenCentralPassword").orNull)
                    stagingRepositories.add("journey-builder/build/staging-deploy")
                    stagingRepositories.add("journey-builder-ksp/build/staging-deploy")
                }
            }
        }
    }
}
