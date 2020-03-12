package AE_FileWatcherExtension

import AE_FileWatcherExtension.buildTypes.*
import AE_FileWatcherExtension.vcsRoots.AE_FileWatcherExtension
import jetbrains.buildServer.configs.kotlin.v2018_2.Project
import jetbrains.buildServer.configs.kotlin.v2018_2.projectFeatures.VersionedSettings.BuildSettingsMode.PREFER_SETTINGS_FROM_VCS
import jetbrains.buildServer.configs.kotlin.v2018_2.projectFeatures.VersionedSettings.Format.KOTLIN
import jetbrains.buildServer.configs.kotlin.v2018_2.projectFeatures.VersionedSettings.Mode.ENABLED
import jetbrains.buildServer.configs.kotlin.v2018_2.projectFeatures.versionedSettings

object Project : Project({
    uuid = "be490abc-6381-4938-92dc-d7b25647ed88"
    id("AE_FileWatcherExtension")
    parentId("AE")
    name = "AE_FileWatcherExtension"

    vcsRoot(AE_FileWatcherExtension)
    buildType(AE_FileWatcherExtension_Build)
    buildType(AE_FileWatcherExtension_IntegrationTests)

    features {
        versionedSettings {
            mode = ENABLED
            buildSettingsMode = PREFER_SETTINGS_FROM_VCS
            rootExtId = "${AE_FileWatcherExtension.id}"
            showChanges = true
            settingsFormat = KOTLIN
            storeSecureParamsOutsideOfVcs = true
        }
    }

    buildTypesOrder = arrayListOf(
            AE_FileWatcherExtension_Build,
            AE_FileWatcherExtension_IntegrationTests
    )
})