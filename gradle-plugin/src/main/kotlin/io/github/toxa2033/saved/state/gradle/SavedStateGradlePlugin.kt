package io.github.toxa2033.saved.state.gradle

import io.github.toxa____.saved_state.gradle_plugin.BuildConfig
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

class SavedStateGradlePlugin : KotlinCompilerPluginSupportPlugin {

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
        return kotlinCompilation.target.project.plugins.hasPlugin(SavedStateGradlePlugin::class.java)
    }

    override fun getCompilerPluginId(): String = PLUGIN_ID

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = "io.github.toxa2033.saved-state",
        artifactId = PLUGIN_ID,
        version = BuildConfig.RELEASE_VERSION
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
            "io.github.toxa2033.saved-state:annotation-core:${BuildConfig.RELEASE_VERSION}"
        )

        return project.provider { emptyList() }
    }
}

open class SavedStateGradleExtension
