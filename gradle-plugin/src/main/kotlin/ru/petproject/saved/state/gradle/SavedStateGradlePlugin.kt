package ru.petproject.saved.state.gradle

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.ArtifactRepositoryContainer
import org.gradle.api.artifacts.repositories.ArtifactRepository
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.artifacts.repositories.RepositoryContentDescriptor
import org.gradle.api.artifacts.repositories.UrlArtifactRepository
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption
import java.net.URI

class SavedStateGradlePlugin : KotlinCompilerPluginSupportPlugin {

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
        return kotlinCompilation.target.project.plugins.hasPlugin(SavedStateGradlePlugin::class.java)
    }

    override fun getCompilerPluginId(): String = PLUGIN_ID

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = "ru.petproject",
        artifactId = PLUGIN_ID,
        version = VERSION
    )

    override fun apply(target: Project) {
        target.extensions.create(PLUGIN_NAME, SavedStateGradleExtension::class.java)
    }

    override fun applyToCompilation(
        kotlinCompilation: KotlinCompilation<*>
    ): Provider<List<SubpluginOption>> {

        val project = kotlinCompilation.target.project
        project.dependencies.add(
            "implementation",
            "ru.petproject.saved.state:saved-state-core:$VERSION"
        )

        return project.provider { emptyList() }
    }
}

open class SavedStateGradleExtension
