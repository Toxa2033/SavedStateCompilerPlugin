package ru.petproject.saved.state.compiler.extensions

import org.jetbrains.kotlin.backend.common.ClassLoweringPass
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.fields

open class IrGenerationSavedStateExtension : IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        IrGenerationSavedStateExtensionPass(pluginContext).lower(moduleFragment)
    }
}

private class IrGenerationSavedStateExtensionPass(
    private val pluginContext: IrPluginContext
) : ClassLoweringPass {

    override fun lower(irClass: IrClass) {
        SavedStateIrGenerator(pluginContext).addSaveStateMethodIfNeeded(irClass)
    }
}
