package AE_LogMonitoringExtension.buildTypes

import AE_LogMonitoringExtension.publishCommitStatus
import AE_LogMonitoringExtension.vcsRoots.AE_LogMonitoringExtension
import AE_LogMonitoringExtension.withDefaults
import jetbrains.buildServer.configs.kotlin.v2018_2.BuildStep
import jetbrains.buildServer.configs.kotlin.v2018_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2018_2.FailureAction
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.exec
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.vcs

object AE_LogMonitoringExtension_IntegrationTests : BuildType({
    uuid = "292f528a-c76d-4653-a09a-e2bf161098dc"
    name = "Run Integration Tests"

    withDefaults()

    steps {
        exec {
            path = "make"
            arguments = "dockerRun sleep"
        }
        maven {
            goals = "clean verify -DskipITs=false"
            mavenVersion = defaultProvidedVersion()
            jdkHome = "%env.JDK_18%"
        }
        exec {
            path = "make"
            arguments = "dockerStop"
        }
        exec {
            executionMode = BuildStep.ExecutionMode.ALWAYS
            path = "make"
            arguments = "dockerClean"
        }
    }

    triggers {
        vcs {
        }
    }

    dependencies {
        dependency(AE_LogMonitoringExtension_Build) {
            snapshot {
                onDependencyFailure = FailureAction.FAIL_TO_START
            }
            artifacts {
                artifactRules = """
                +:target/LogMonitor-*.zip => target/
            """.trimIndent()
            }
        }
    }

    publishCommitStatus()
})