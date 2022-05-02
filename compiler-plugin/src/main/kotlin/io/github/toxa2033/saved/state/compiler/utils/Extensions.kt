package io.github.toxa2033.saved.state.compiler.utils

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.resolve.descriptorUtil.classId
import org.jetbrains.kotlin.resolve.descriptorUtil.getAllSuperClassifiers
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.typeUtil.isInterface
import org.jetbrains.kotlin.types.typeUtil.supertypes
import java.util.*

fun IrPluginContext.blockBody(
    symbol: IrSymbol,
    block: IrBlockBodyBuilder.() -> Unit
): IrBlockBody = DeclarationIrBuilder(this, symbol).irBlockBody { block() }

fun KotlinType.getNameType(): String? = constructor.declarationDescriptor?.name?.asString()
fun KotlinType.getClassIdType() = constructor.declarationDescriptor?.classId

fun KotlinType.getTypeIfTypeFromLiveData(): KotlinType {
    var type: KotlinType = this
    val typeConstructor = type.constructor.declarationDescriptor?.getAllSuperClassifiers()
    if (typeConstructor?.any { it.classId == LIVE_DATA_CLASS } == true) {
        type = type.arguments.firstOrNull()?.type ?: error("Not found argument type from LiveData")
    }
    return type
}

//TODO: пропускает конструкции типа List<Неподдерживаемый тип>
fun KotlinType?.isSupportedType(): Boolean {
    if (this == null) return false

    return this.getClassIdType() in supportedType
}

fun KotlinType?.hasSupportedInterface(): Boolean {
    if (this == null) return false
    return this.supertypes().any { it.isInterface() && it.getClassIdType() in supportedDeserializeInterface }
}

fun String.capitalize() = replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }