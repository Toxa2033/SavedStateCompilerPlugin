package io.github.toxa2033.saved.state.compiler.utils

import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.diagnostics.DiagnosticFactory0
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.diagnostics.Severity
import org.jetbrains.kotlin.diagnostics.rendering.DefaultErrorMessages
import org.jetbrains.kotlin.diagnostics.rendering.DiagnosticFactoryToRendererMap
import io.github.toxa2033.saved.state.compiler.utils.UnsupportedTypeErrors.NOT_FOUND_SAVED_STATE_HANDLER_PROPERTY
import io.github.toxa2033.saved.state.compiler.utils.UnsupportedTypeErrors.THIS_TYPE_NOT_SUPPORTED

object UnsupportedTypeErrors {
    @JvmField
    val THIS_TYPE_NOT_SUPPORTED =
        DiagnosticFactory0.create<PsiElement>(Severity.ERROR)

    @JvmField
    val NOT_FOUND_SAVED_STATE_HANDLER_PROPERTY =
        DiagnosticFactory0.create<PsiElement>(Severity.ERROR)

    init {
        Errors.Initializer.initializeFactoryNamesAndDefaultErrorMessages(
            UnsupportedTypeErrors::class.java,
            PluginErrorsRendering
        )
    }
}

object PluginErrorsRendering : DefaultErrorMessages.Extension {
    private val _map = DiagnosticFactoryToRendererMap("SavedStateHandle Plugin")
    override fun getMap(): DiagnosticFactoryToRendererMap = _map



    init {
        _map.apply {
            put(THIS_TYPE_NOT_SUPPORTED, UNSUPPORTED_TYPE_ERROR)
            put(NOT_FOUND_SAVED_STATE_HANDLER_PROPERTY, NOT_FOUND_SAVED_STATE_HANDLER_PROPERTY_ERROR)
        }
    }
}