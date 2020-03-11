package AE_FileWatcherExtension.vcsRoots

import jetbrains.buildServer.configs.kotlin.v2018_2.vcs.GitVcsRoot

object AE_FileWatcherExtension : GitVcsRoot({
    uuid = "8a28bed7-6be0-4101-89fd-78586c44df2f"
    id("AE_FileWatcherExtension")
    name = "AE_FileWatcherExtension"
    url = "ssh://git@bitbucket.corp.appdynamics.com:7999/ae/file-monitoring-extension.git"
    pushUrl = "ssh://git@bitbucket.corp.appdynamics.com:7999/ae/file-monitoring-extension.git"
    authMethod = uploadedKey {
        uploadedKey = "TeamCity BitBucket Key"
    }
    agentCleanPolicy = AgentCleanPolicy.ALWAYS
    branchSpec = """
    +:refs/heads/(master)
    +:refs/(pull-requests/*)/from
    """.trimIndent()
})