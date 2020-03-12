package AE_FileWatcherExtension.buildTypes

import AE_FileWatcherExtension.publishCommitStatus
import AE_FileWatcherExtension.vcsRoots.AE_FileWatcherExtension
import AE_FileWatcherExtension.withDefaults
import jetbrains.buildServer.configs.kotlin.v2018_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.vcs

object AE_FileWatcherExtension_Build : BuildType({
    uuid = "0d57ff72-2662-4184-9564-b5009a9fd7af"
    name = "File Watcher Extension Build"

    withDefaults()

    steps {
        maven {
            goals = "clean install"
            mavenVersion = defaultProvidedVersion()
            jdkHome = "%env.JDK_18%"
            userSettingsSelection = "teamcity-settings"
        }
    }

    triggers {
        vcs {
        }
    }

    artifactRules = """
    +:target/FileWatcher-*.zip => target/
""".trimIndent()

    publishCommitStatus()
})