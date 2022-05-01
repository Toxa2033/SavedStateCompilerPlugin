package ru.petproject.saved.state.compiler.extensions

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.jvm.ir.defaultValue
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.descriptors.IrBasedValueParameterDescriptor
import org.jetbrains.kotlin.ir.descriptors.toIrBasedDescriptor
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.interpreter.toIrConst
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor
import org.jetbrains.kotlin.name.Name
import ru.petproject.saved.state.compiler.extensions.SavedStateResolveExtension.Companion.GET
import ru.petproject.saved.state.compiler.extensions.SavedStateResolveExtension.Companion.GET_IDENTIFIER_START
import ru.petproject.saved.state.compiler.extensions.SavedStateResolveExtension.Companion.SET
import ru.petproject.saved.state.compiler.extensions.SavedStateResolveExtension.Companion.VALUE
import ru.petproject.saved.state.compiler.utils.*
import ru.petproject.saved.state.compiler.utils.FqNames.SAVE_STATE_ANNOTATION_NAME

class SavedStateIrGenerator(
    private val pluginContext: IrPluginContext
) {

    companion object {
        private const val METHOD_GET_LIVE_DATA = "getLiveData"
    }

    fun addSaveStateMethodIfNeeded(irClass: IrClass) {
        val propertyWithAnnotation = irClass.properties
            .filter { it.annotations.hasAnnotation(SAVE_STATE_ANNOTATION_NAME) }
            .map { it.name.asString() }

        if(propertyWithAnnotation.count() == 0) return

        val stateHandleProperty =
            irClass.properties.find { it.backingField?.type?.getClass()?.classId == TYPE_STATE_HANDLER }
                ?: error("Not found SavedStateHandle property")

        val functionsGenerated = mutableMapOf<IrSimpleFunction, String>()

        irClass.functions.forEach { func ->
            val property = propertyWithAnnotation.find { func.name.asString().contains(it, ignoreCase = true) }
            if (property != null && func.body == null) {
                functionsGenerated[func] = property
            }
        }

        functionsGenerated.forEach { (func, propertyName) ->
            val funName = func.name.asString()
            val capPropName = propertyName.capitalize()
            val getLiveDataNameMethod = "$GET$capPropName$LIVE_DATA"
            val getValueNameMethod = "$GET$capPropName$VALUE"
            val setValueNameMethod = "$SET$capPropName$VALUE"
            val getIdentifierNameMethod = "$GET_IDENTIFIER_START$capPropName"
            when(funName) {
                getLiveDataNameMethod -> func.fillGetLiveData(stateHandleProperty, propertyName)
                getValueNameMethod -> func.fillGetValue(stateHandleProperty, propertyName)
                getIdentifierNameMethod -> func.fillGetNameIdentifier(propertyName)
                setValueNameMethod -> func.fillSet(stateHandleProperty, propertyName)
                else -> println("Method cant filled")
            }
        }

        println("--------")
        println(irClass.dump())
        println("--------")
        println(irClass.dumpKotlinLike())

    }

    /**
    FUN name:setTest visibility:public modality:FINAL <> ($this:<root>.Soma, key:kotlin.String, value:kotlin.String?) returnType:kotlin.Unit
    $this: VALUE_PARAMETER name:<this> type:<root>.Soma
    VALUE_PARAMETER name:key index:0 type:kotlin.String
    VALUE_PARAMETER name:value index:1 type:kotlin.String?
    BLOCK_BODY
        CALL 'public final fun set <T> (key: kotlin.String, value: T of androidx.lifecycle.SavedStateHandle.set?): kotlin.Unit [operator] declared in androidx.lifecycle.SavedStateHandle' type=kotlin.Unit origin=null
            <T>: kotlin.String
            $this: CALL 'private final fun <get-savedStateHandle> (): androidx.lifecycle.SavedStateHandle declared in <root>.Soma' type=androidx.lifecycle.SavedStateHandle origin=GET_PROPERTY
                $this: GET_VAR '<this>: <root>.Soma declared in <root>.Soma.setTest' type=<root>.Soma origin=null
            key: GET_VAR 'key: kotlin.String declared in <root>.Soma.setTest' type=kotlin.String origin=null
            value: GET_VAR 'value: kotlin.String? declared in <root>.Soma.setTest' type=kotlin.String? origin=null
     */
    private fun IrSimpleFunction.fillSet(saveStateProperty: IrProperty, originalPropertyName: String) {
        val funDispatchReceiverParameter = dispatchReceiverParameter ?: notFoundDispatcherParameters(name)
        val getterSavedState = saveStateProperty.getter ?: notFoundGetterError(saveStateProperty.name)
        val setFunction = saveStateProperty.findFunctionByNameFromClassProperty(SET)
        val argumentFunction = valueParameters.firstOrNull() ?: error("Not found argument of function $name")

        val blockBody = pluginContext.blockBody(symbol) {
            +irCall(setFunction).also { call ->
                call.putTypeArgument(0, argumentFunction.type)
                call.dispatchReceiver = irCall(getterSavedState).also {
                    it.dispatchReceiver = irGet(funDispatchReceiverParameter)
                }

                call.putValueArgument(0, irString(originalPropertyName))
                call.putValueArgument(1, irGet(argumentFunction))
            }
        }
        body = blockBody
    }


    /**
    FUN name:getLiveData visibility:public modality:FINAL <> ($this:<root>.Test, default:kotlin.String?) returnType:androidx.lifecycle.MutableLiveData<kotlin.String>
        $this: VALUE_PARAMETER name:<this> type:<root>.Test
        VALUE_PARAMETER name:default index:0 type:kotlin.String?
            EXPRESSION_BODY
                CONST Null type=kotlin.Nothing? value=null
        BLOCK_BODY
            RETURN type=kotlin.Nothing from='public final fun getLiveData (default: kotlin.String?): androidx.lifecycle.MutableLiveData<kotlin.String> declared in <root>.Test'
                CALL 'public final fun getLiveData <T> (key: kotlin.String, initialValue: T of androidx.lifecycle.SavedStateHandle.getLiveData?): androidx.lifecycle.MutableLiveData<T of androidx.lifecycle.SavedStateHandle.getLiveData> declared in androidx.lifecycle.SavedStateHandle' type=androidx.lifecycle.MutableLiveData<kotlin.String> origin=null
                    <T>: kotlin.String
                    $this: CALL 'private final fun <get-savedStateHandle> (): androidx.lifecycle.SavedStateHandle declared in <root>.Test' type=androidx.lifecycle.SavedStateHandle origin=GET_PROPERTY
                        $this: GET_VAR '<this>: <root>.Test declared in <root>.Test.getLiveData' type=<root>.Test origin=null
                    key: CONST String type=kotlin.String value="phone"
                    initialValue: GET_VAR 'default: kotlin.String? declared in <root>.Test.getLiveData' type=kotlin.String? origin=null
     * */
    private fun IrSimpleFunction.fillGetLiveData(saveStateProperty: IrProperty, originalPropertyName: String) {
        val funDispatchReceiverParameter = dispatchReceiverParameter ?: notFoundDispatcherParameters(name)
        val getterSavedState = saveStateProperty.getter ?: notFoundGetterError(saveStateProperty.name)
        val getFunction = saveStateProperty.findFunctionByNameFromClassProperty(METHOD_GET_LIVE_DATA, countArgs = 2)
        val isLiveData = returnTypeIsLiveData()

        val valueParam = valueParameters.firstOrNull() ?: error("Not found default value for auto generated get${originalPropertyName}LiveData method")

        val typeValueParameter = valueParam.defaultValue?.expression?.type ?: error("Not found default value in get${originalPropertyName}LiveData")

        val expressionBodyDefaultValue = pluginContext.irFactory.createExpressionBody(startOffset, endOffset) {
            expression = IrConstImpl(startOffset, endOffset, typeValueParameter, IrConstKind.Null, null)
        }

        valueParameters.firstOrNull()?.defaultValue = expressionBodyDefaultValue


        val typeArgument = if(isLiveData) returnType.getArguments()?.firstOrNull() else returnType
        val blockBody = pluginContext.blockBody(symbol) {
            val call = irCall(getFunction).also { call ->
                call.putValueArgument(0, irString(originalPropertyName))
                call.putValueArgument(1, irGet(valueParam))
                call.putTypeArgument(0, typeArgument)
                call.dispatchReceiver = irCall(getterSavedState).also {
                    it.dispatchReceiver = irGet(funDispatchReceiverParameter)
                }
            }
            +irReturn(call)
        }
        body = blockBody
    }

/**
FUN name:testMethod visibility:public modality:FINAL <> ($this:<root>.Soma) returnType:kotlin.String?
$this: VALUE_PARAMETER name:<this> type:<root>.Soma
BLOCK_BODY
    RETURN type=kotlin.Nothing from='public final fun testMethod (): kotlin.String? declared in <root>.Soma'
        CALL 'public final fun get <T> (name: kotlin.String): T of <root>.Soma.get? declared in <root>.Soma' type=kotlin.String? origin=null
            <T>: kotlin.String
            $this: CALL 'private final fun <get-map> (): kotlin.collections.MutableMap<kotlin.String, kotlin.Any> declared in <root>.Soma' type=kotlin.collections.MutableMap<kotlin.String, kotlin.Any> origin=GET_PROPERTY
                $this: GET_VAR '<this>: <root>.Soma declared in <root>.Soma.testMethod' type=<root>.Soma origin=null
            name: CONST String type=kotlin.String value="soba"
 * */
    private fun IrSimpleFunction.fillGetValue(saveStateProperty: IrProperty, originalPropertyName: String) {
        val funDispatchReceiverParameter = dispatchReceiverParameter ?: notFoundDispatcherParameters(name)
        val getterSavedState = saveStateProperty.getter ?: notFoundGetterError(saveStateProperty.name)
        val getFunction = saveStateProperty.findFunctionByNameFromClassProperty(GET)
        val isLiveData = returnTypeIsLiveData()

        val typeArgument = if(isLiveData) returnType.getArguments()?.firstOrNull() else returnType

        val blockBody = pluginContext.blockBody(symbol) {
            val call = irCall(getFunction).also { call ->
                call.putValueArgument(0, irString(originalPropertyName))
                call.putTypeArgument(0, typeArgument?.makeNotNull())
                call.dispatchReceiver = irCall(getterSavedState).also {
                    it.dispatchReceiver = irGet(funDispatchReceiverParameter)
                }
            }
            +irReturn(call)
        }
        body = blockBody
    }

    /**
    PROPERTY name:TEST visibility:private modality:FINAL [val]
        FUN name:<get-TEST> visibility:private modality:FINAL <> ($this:<root>.Soma) returnType:kotlin.String
            correspondingProperty: PROPERTY name:TEST visibility:private modality:FINAL [val]
            $this: VALUE_PARAMETER name:<this> type:<root>.Soma
            BLOCK_BODY
                RETURN type=kotlin.Nothing from='private final fun <get-TEST> (): kotlin.String declared in <root>.Soma'
                    CONST String type=kotlin.String value="soba"
     */

    private fun IrProperty.fillProperty(originalPropertyName: String) {
        val getterNonNull = getter ?: notFoundGetterError(name)
        val block = pluginContext.blockBody(getterNonNull.symbol) {
            +irReturn(irString(originalPropertyName))
        }
        getterNonNull.body = block
    }


    private fun IrSimpleFunction.fillGetNameIdentifier(originalPropertyName: String) {
        val blockBody = pluginContext.blockBody(symbol) {
            val propertyName = irString(originalPropertyName)
            +irReturn(propertyName)
        }
        body = blockBody
    }

    private fun IrProperty.findFunctionByNameFromClassProperty(
        name:String, countArgs: Int? = null
    ): IrSimpleFunction {
        return backingField?.type?.classOrNull?.functions?.firstOrNull { func ->
            func.owner.name.asString() == name && (countArgs == null || func.owner.valueParameters.size == countArgs)
        }?.owner
            ?: error("Not Found function $name in class ${this.name}")
    }

    private fun IrType.getArguments(): ArrayList<IrType>? {
        return javaClass.getDeclaredField("arguments").let {
            it.isAccessible = true
            val value = it.get(this)
            return@let value as? ArrayList<IrType>
        }
    }

    private fun IrSimpleFunction.returnTypeIsLiveData(): Boolean {
        return returnType.superTypes().any { it.getClass()?.classId == LIVE_DATA_CLASS }
                || returnType.getClass()?.classId == LIVE_DATA_CLASS
    }
    private fun notFoundGetterError(propertyName: Name): Nothing {
        error("Not found getter in property $propertyName")
    }

    private fun notFoundDispatcherParameters(name: Name): Nothing {
        error("Not found dispatchReceiverParameter from $name")
    }

}