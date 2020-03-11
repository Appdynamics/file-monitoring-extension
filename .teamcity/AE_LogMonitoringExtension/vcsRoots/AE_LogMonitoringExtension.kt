package AE_LogMonitoringExtension.vcsRoots

import jetbrains.buildServer.configs.kotlin.v2018_2.vcs.GitVcsRoot

object AE_LogMonitoringExtension : GitVcsRoot({
    uuid = "ff7c22be-db8e-4ae6-bc75-4f298b42cfe2"
    id("AE_LogMonitoringExtension")
    name = "AE_LogMonitoringExtension"
    url = "ssh://git@bitbucket.corp.appdynamics.com:7999/ae/log-monitoring-extension.git"
    pushUrl = "ssh://git@bitbucket.corp.appdynamics.com:7999/ae/log-monitoring-extension.git"
    authMethod = uploadedKey {
        uploadedKey = "TeamCity BitBucket Key"
    }
    agentCleanPolicy = AgentCleanPolicy.ALWAYS
    branchSpec = """
    +:refs/heads/(master)
    +:refs/(pull-requests/*)/from
    """.trimIndent()
})