package io.github.toxa2033.saved.state.compiler.extensions

import io.github.toxa2033.saved.state.compiler.utils.*
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.impl.referencedProperty
import org.jetbrains.kotlin.diagnostics.DiagnosticFactory0
import org.jetbrains.kotlin.js.resolve.diagnostics.findPsi
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.resolve.BindingTrace
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.checkers.DeclarationChecker
import org.jetbrains.kotlin.resolve.checkers.DeclarationCheckerContext

class SavedStateDeclarationChecker : DeclarationChecker {
    override fun check(
        declaration: KtDeclaration,
        descriptor: DeclarationDescriptor,
        context: DeclarationCheckerContext
    ) {
        if (descriptor !is PropertyDescriptor) return
        if (descriptor.annotations.hasAnnotation(FqNames.SAVE_STATE_ANNOTATION_NAME).not()) return

        val descriptorClass = descriptor.getTopLevelContainingClassifier()
        if(descriptorClass is ClassDescriptor) {
            val members = descriptorClass.defaultType.memberScope
            val variables = members.getVariableNames()
            val hasStateHandler = variables.any { variable ->
                val property = kotlin.runCatching { DescriptorUtils.getPropertyByName(members, variable) }.getOrNull()
                property?.returnType?.getClassIdType() == TYPE_STATE_HANDLER
            }
            if(hasStateHandler.not()) {
                context.trace.report(
                    descriptorClass,
                    UnsupportedTypeErrors.NOT_FOUND_SAVED_STATE_HANDLER_PROPERTY
                )
            }
        }

        val typeProperty = descriptor.referencedProperty?.returnType?.getTypeIfTypeFromLiveData()

        if (typeProperty.isSupportedType().not() && typeProperty.hasSupportedInterface().not()) {
            context.trace.report(
                descriptor,
                UnsupportedTypeErrors.THIS_TYPE_NOT_SUPPORTED
            )
        }
        println("Name type property - ${typeProperty?.getNameType()}")
    }

    private fun BindingTrace.report(
        descriptor: DeclarationDescriptorNonRoot,
        error: DiagnosticFactory0<PsiElement>
    ) {
        descriptor.findPsi()?.let { report(error.on(it)) }
    }
}