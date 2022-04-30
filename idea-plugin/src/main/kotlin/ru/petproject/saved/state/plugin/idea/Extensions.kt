package ru.petproject.saved.state.plugin.idea

import ru.petproject.saved.state.compiler.extensions.IrGenerationSavedStateExtension
import ru.petproject.saved.state.compiler.extensions.SavedStateResolveExtension

open class SynthResolveExtension: SavedStateResolveExtension()

open class IrGeneration: IrGenerationSavedStateExtension()