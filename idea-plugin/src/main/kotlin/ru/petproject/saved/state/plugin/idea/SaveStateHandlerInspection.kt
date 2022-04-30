package ru.petproject.saved.state.plugin.idea

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import org.jetbrains.kotlin.idea.caches.resolve.resolveToDescriptorIfAny
import org.jetbrains.kotlin.idea.inspections.AbstractKotlinInspection
import org.jetbrains.kotlin.idea.structuralsearch.resolveKotlinType
import org.jetbrains.kotlin.psi.allConstructors
import org.jetbrains.kotlin.psi.classVisitor
import ru.petproject.saved.state.compiler.utils.FqNames
import ru.petproject.saved.state.compiler.utils.NOT_FOUND_SAVED_STATE_HANDLER_PROPERTY_ERROR
import ru.petproject.saved.state.compiler.utils.TYPE_STATE_HANDLER
import ru.petproject.saved.state.compiler.utils.getClassIdType

class SaveStateHandlerInspection : AbstractKotlinInspection() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return classVisitor { ktClass ->

            val hasAnnotatedProperty = ktClass.getProperties().any { property ->
                property.modifierList?.annotationEntries?.any { annotationEntry ->
                    annotationEntry.resolveToDescriptorIfAny()?.fqName == FqNames.SAVE_STATE_ANNOTATION_NAME
                } ?: false
            }

            if (hasAnnotatedProperty.not()) return@classVisitor

            val hasConstructorProperty = ktClass.allConstructors.any { constructor ->
                constructor.valueParameters.any { param ->
                    param.resolveKotlinType()?.getClassIdType() == TYPE_STATE_HANDLER && param.hasValOrVar()
                }
            }
            val hasClassProperty =
                ktClass.getProperties().any { it.resolveKotlinType()?.getClassIdType() == TYPE_STATE_HANDLER }

            if (hasClassProperty.not() && hasConstructorProperty.not()) {
                val classIdentifier = ktClass.nameIdentifier ?: return@classVisitor
                val problemNotFoundDescriptor = holder.manager.createProblemDescriptor(
                    classIdentifier,
                    classIdentifier,
                    NOT_FOUND_SAVED_STATE_HANDLER_PROPERTY_ERROR,
                    ProblemHighlightType.GENERIC_ERROR,
                    isOnTheFly
                )
                holder.registerProblem(problemNotFoundDescriptor)
            }

        }
    }
}
