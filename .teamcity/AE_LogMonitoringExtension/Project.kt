package AE_LogMonitoringExtension

import AE_LogMonitoringExtension.buildTypes.*
import AE_LogMonitoringExtension.vcsRoots.AE_LogMonitoringExtension
import jetbrains.buildServer.configs.kotlin.v2018_2.Project
import jetbrains.buildServer.configs.kotlin.v2018_2.projectFeatures.VersionedSettings.BuildSettingsMode.PREFER_SETTINGS_FROM_VCS
import jetbrains.buildServer.configs.kotlin.v2018_2.projectFeatures.VersionedSettings.Format.KOTLIN
import jetbrains.buildServer.configs.kotlin.v2018_2.projectFeatures.VersionedSettings.Mode.ENABLED
import jetbrains.buildServer.configs.kotlin.v2018_2.projectFeatures.versionedSettings

object Project : Project({
    uuid = "30e4fffc-c422-47ae-b31a-9efe30d3d680"
    id("AE_LogMonitoringExtension")
    parentId("AE")
    name = "AE_LogMonitoringExtension"

    vcsRoot(AE_LogMonitoringExtension)
    buildType(AE_LogMonitoringExtension_Build)
    buildType(AE_LogMonitoringExtension_IntegrationTests)

    features {
        versionedSettings {
            mode = ENABLED
            buildSettingsMode = PREFER_SETTINGS_FROM_VCS
            rootExtId = "${AE_LogMonitoringExtension.id}"
            showChanges = true
            settingsFormat = KOTLIN
            storeSecureParamsOutsideOfVcs = true
        }
    }

    buildTypesOrder = arrayListOf(
            AE_LogMonitoringExtension_Build,
            AE_LogMonitoringExtension_IntegrationTests
    )
})