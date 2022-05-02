package io.github.toxa2033.saved.state.plugin.idea

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import io.github.toxa2033.saved.state.compiler.utils.*
import org.jetbrains.kotlin.idea.caches.resolve.resolveToDescriptorIfAny
import org.jetbrains.kotlin.idea.inspections.AbstractKotlinInspection
import org.jetbrains.kotlin.idea.structuralsearch.resolveKotlinType
import org.jetbrains.kotlin.psi.propertyVisitor

class AnnotatedUnsupportedTypeInspection : AbstractKotlinInspection() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return propertyVisitor { property ->

            val annotationsEntry = property.modifierList?.annotationEntries

            val hasAnnotation = annotationsEntry?.any {
                it.resolveToDescriptorIfAny()?.fqName == FqNames.SAVE_STATE_ANNOTATION_NAME
            } ?: false

            if (hasAnnotation.not()) return@propertyVisitor
            val type = property.resolveKotlinType()?.getTypeIfTypeFromLiveData()

            if (type.isSupportedType().not() && type.hasSupportedInterface().not()) {
                val identifier = property.nameIdentifier ?: return@propertyVisitor
                val problemDescriptor = holder.manager.createProblemDescriptor(
                    identifier,
                    identifier,
                    UNSUPPORTED_TYPE_ERROR,
                    ProblemHighlightType.GENERIC_ERROR,
                    isOnTheFly
                )
                holder.registerProblem(problemDescriptor)
            }
        }
    }
}
