package io.github.toxa2033.saved.state.compiler

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.container.StorageComponentContainer
import org.jetbrains.kotlin.container.useInstance
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.extensions.StorageComponentContainerContributor
import org.jetbrains.kotlin.platform.TargetPlatform
import org.jetbrains.kotlin.resolve.extensions.SyntheticResolveExtension
import io.github.toxa2033.saved.state.compiler.extensions.IrGenerationSavedStateExtension
import io.github.toxa2033.saved.state.compiler.extensions.SavedStateDeclarationChecker
import io.github.toxa2033.saved.state.compiler.extensions.SavedStateResolveExtension

class SavedStateComponentRegistrar : ComponentRegistrar {
    override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {

        SyntheticResolveExtension.registerExtension(project, SavedStateResolveExtension())

        StorageComponentContainerContributor.registerExtension(
            project,
            object : StorageComponentContainerContributor {
                override fun registerModuleComponents(
                    container: StorageComponentContainer,
                    platform: TargetPlatform,
                    moduleDescriptor: ModuleDescriptor
                ) {
                    container.useInstance(SavedStateDeclarationChecker())
                }
            }
        )

        IrGenerationExtension.registerExtension(project, IrGenerationSavedStateExtension())
    }
}