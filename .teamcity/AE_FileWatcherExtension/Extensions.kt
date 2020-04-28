package AE_FileWatcherExtension

import AE_FileWatcherExtension.vcsRoots.AE_FileWatcherExtension
import jetbrains.buildServer.configs.kotlin.v2018_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2018_2.VcsRoot
import jetbrains.buildServer.configs.kotlin.v2018_2.buildFeatures.commitStatusPublisher
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.finishBuildTrigger
import jetbrains.buildServer.configs.kotlin.v2018_2.BuildTypeSettings

fun BuildType.publishCommitStatus() {
    features {
        commitStatusPublisher {
            vcsRootExtId = "${AE_FileWatcherExtension.id}"
            publisher = bitbucketServer {
                url = "%env.BITBUCKET_SERVER%"
                userName = "%env.BITBUCKET_USERNAME%"
                password = "%env.BITBUCKET_PASSWORD%"
            }
        }
    }
}

fun BuildType.withDefaults() {
    vcs {
        root(AE_FileWatcherExtension)
        cleanCheckout = true
    }

    requirements {
        matches("env.AGENT_OS", "Linux")
    }
}

fun BuildTypeSettings.triggerAfter(buildJob: BuildTypeSettings, branches: String = "+:master") {
    triggers {
        finishBuildTrigger {
            buildType = "${buildJob.id}"
            successfulOnly = true
            branchFilter = branches
        }
    }
}