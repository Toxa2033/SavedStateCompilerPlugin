package io.github.toxa2033.saved.state.gradle

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

    private val version = System.getenv(RELEASE_VERSION_CONST) ?: VERSION_DEFAULT

    override fun getCompilerPluginId(): String = PLUGIN_ID

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = "io.github.toxa2033.saved-state",
        artifactId = PLUGIN_ID,
        version = version
    )

    override fun apply(target: Project) {
        target.extensions.create(PLUGIN_NAME, SavedStateGradleExtension::class.java)
    }

    override fun applyToCompilation(
        kotlinCompilation: KotlinCompilation<*>
    ): Provider<List<SubpluginOption>> {
        println("VERSION OF IMPL LIB: $version")
        val project = kotlinCompilation.target.project
        project.dependencies.add(
            "implementation",
            "io.github.toxa2033.saved-state:annotation-core:$version"
        )

        return project.provider { emptyList() }
    }
}

open class SavedStateGradleExtension
