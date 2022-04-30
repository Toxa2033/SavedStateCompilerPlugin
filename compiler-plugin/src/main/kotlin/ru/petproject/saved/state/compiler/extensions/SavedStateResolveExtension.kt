package ru.petproject.saved.state.compiler.extensions

import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.SimpleFunctionDescriptorImpl
import org.jetbrains.kotlin.descriptors.impl.ValueParameterDescriptorImpl
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.descriptorUtil.builtIns
import org.jetbrains.kotlin.resolve.descriptorUtil.classId
import org.jetbrains.kotlin.resolve.descriptorUtil.module
import org.jetbrains.kotlin.resolve.extensions.SyntheticResolveExtension
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.KotlinTypeFactory
import org.jetbrains.kotlin.types.typeUtil.asTypeProjection
import org.jetbrains.kotlin.types.typeUtil.makeNullable
import ru.petproject.saved.state.compiler.utils.*
import ru.petproject.saved.state.compiler.utils.FqNames.SAVE_STATE_ANNOTATION_NAME
import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque

open class SavedStateResolveExtension : SyntheticResolveExtension {

    companion object {
        const val DEFAULT_NAME = "default"
        const val GET_IDENTIFIER_START = "getIdentifier"
        const val GET = "get"
        const val SET = "set"
        const val VALUE = "Value"
    }

    private val listPropertyDescriptionData = ConcurrentLinkedDeque<PropertyDescriptionData>()


    override fun generateSyntheticMethods(
        thisDescriptor: ClassDescriptor,
        name: Name,
        bindingContext: BindingContext,
        fromSupertypes: List<SimpleFunctionDescriptor>,
        result: MutableCollection<SimpleFunctionDescriptor>
    ) {
        val propertyDescriptionData = listPropertyDescriptionData.find { name in it.creations } ?: return
        println("FUNCTION CREATED - $name" +
                "\nClass - ${thisDescriptor.name}" +
                " list method ${listPropertyDescriptionData.map { it.creations }}"
        )

        val method = when {
            name.asString().startsWith(GET_IDENTIFIER_START) -> createGetIdentifierMethod(thisDescriptor, name)
            name.asString().startsWith(GET) -> {
                createGetMethodDescriptor(
                    thisDescriptor,
                    name,
                    propertyDescriptionData,
                    name.asString().contains(LIVE_DATA)
                )
            }
            else -> createSetMethodDescriptor(thisDescriptor, name, propertyDescriptionData)
        }

        result.add(method)
    }

    override fun getSyntheticFunctionNames(thisDescriptor: ClassDescriptor): List<Name> {
        val propertyNames = mutableListOf<Name>()
        listPropertyDescriptionData.clear()
        //println("Class - ${thisDescriptor.name}")
        val list = thisDescriptor.flatMapAnnotatedClassProperty { property ->
            val type = property.type.getTypeIfTypeFromLiveData()
            val shouldCreate = type.hasSupportedInterface() || type.isSupportedType()

            //println("Resolve - ${property.name} $type $shouldCreate")
            if (shouldCreate.not()) return@flatMapAnnotatedClassProperty emptyList()

            val fieldName = property.name.asString().capitalize()
            val listMethod = listOf(
                Name.identifier("$SET$fieldName$VALUE"),
                Name.identifier("$GET$fieldName$LIVE_DATA"),
                Name.identifier("$GET$fieldName$VALUE"),
                Name.identifier("$GET_IDENTIFIER_START$fieldName")
            )
            listPropertyDescriptionData.add(PropertyDescriptionData(listMethod, property))
            propertyNames.add(Name.identifier("${property.name.asString()}$IDENTIFIER".uppercase()))
            listMethod
        }
        return list
    }

    private fun ClassDescriptor.flatMapAnnotatedClassProperty(
        flatMap: (property: PropertyDescriptor) -> List<Name>
    ): List<Name> {
        val members = defaultType.memberScope
        val variables = members.getVariableNames()
        return variables.flatMap { variable ->
            val property = kotlin.runCatching { DescriptorUtils.getPropertyByName(members, variable) }.getOrNull()
            if (property?.annotations?.hasAnnotation(SAVE_STATE_ANNOTATION_NAME) == true) {
                flatMap(property)
            } else {
                emptyList()
            }
        }
    }

    private fun createGetIdentifierMethod(
        thisClassDescriptor: ClassDescriptor,
        name: Name
    ): SimpleFunctionDescriptor {
        return SimpleFunctionDescriptorImpl.create(
            thisClassDescriptor,
            Annotations.EMPTY,
            name,
            CallableMemberDescriptor.Kind.SYNTHESIZED,
            thisClassDescriptor.source,
        ).apply {
            initialize(
                null,
                thisClassDescriptor.thisAsReceiverParameter,
                listOf(),
                emptyList(),
                thisClassDescriptor.builtIns.stringType,
                Modality.FINAL,
                DescriptorVisibilities.PRIVATE
            )
        }
    }

    private fun createGetMethodDescriptor(
        thisClassDescriptor: ClassDescriptor,
        name: Name,
        propertyDescriptionData: PropertyDescriptionData,
        isGetLiveData: Boolean
    ): SimpleFunctionDescriptor {
        var type = propertyDescriptionData.property.type
        //если название типа != мутабл лайв дата или не генерим метод где нужна лайвдата, то выбираем первый аргумент
        if (isGetLiveData.not() || type.assertClassId(MUTABLE_LIVE_DATA_CLASS).not()) {
            type = type.getTypeIfTypeFromLiveData()
        }

        var argument: ValueParameterDescriptorImpl? = null

        if (isGetLiveData.not()) {
            type = type.makeNullable()
        } else if (type.assertClassId(MUTABLE_LIVE_DATA_CLASS).not()) {
            val mutableLiveDataClassDescriptor = thisClassDescriptor.mutableLifecycleClassDescriptor

            type = KotlinTypeFactory.simpleNotNullType(
                Annotations.EMPTY,
                mutableLiveDataClassDescriptor,
                listOf(type.asTypeProjection())
            )
            argument = thisClassDescriptor.createDefaultValueArgument(type)
        } else {
            argument = thisClassDescriptor.createDefaultValueArgument(type)
        }
        return SimpleFunctionDescriptorImpl.create(
            thisClassDescriptor,
            Annotations.EMPTY,
            name,
            CallableMemberDescriptor.Kind.SYNTHESIZED,
            thisClassDescriptor.source,
        ).apply {
            initialize(
                null,
                thisClassDescriptor.thisAsReceiverParameter,
                listOf(),
                listOfNotNull(argument),
                type,
                Modality.FINAL,
                DescriptorVisibilities.PRIVATE
            )
        }
    }

    private fun createSetMethodDescriptor(
        thisClassDescriptor: ClassDescriptor,
        name: Name,
        propertyDescriptionData: PropertyDescriptionData
    ): SimpleFunctionDescriptor {
        val type = propertyDescriptionData.property.type.getTypeIfTypeFromLiveData()
        val containingDeclaration = thisClassDescriptor.constructors.firstOrNull()
            ?: error("Not found containingDeclaration for argument of set method")

        val argument = ValueParameterDescriptorImpl(
            containingDeclaration = containingDeclaration,
            original = null,
            index = 0,
            annotations = Annotations.EMPTY,
            name = propertyDescriptionData.property.name,
            outType = type.makeNullable(),
            declaresDefaultValue = false,
            isCrossinline = false,
            isNoinline = false,
            varargElementType = null,
            source = thisClassDescriptor.source,
        )
        return SimpleFunctionDescriptorImpl.create(
            thisClassDescriptor,
            Annotations.EMPTY,
            name,
            CallableMemberDescriptor.Kind.SYNTHESIZED,
            thisClassDescriptor.source,
        ).apply {
            initialize(
                null,
                thisClassDescriptor.thisAsReceiverParameter,
                listOf(),
                listOf(argument),
                thisClassDescriptor.builtIns.unitType,
                Modality.FINAL,
                DescriptorVisibilities.PRIVATE
            )
        }
    }
}

private fun ClassDescriptor.createDefaultValueArgument(type: KotlinType): ValueParameterDescriptorImpl {
    val containingDeclaration = constructors.firstOrNull()
        ?: error("Not found containingDeclaration for argument of set method")
    return ValueParameterDescriptorImpl(
        containingDeclaration = containingDeclaration,
        original = null,
        index = 0,
        annotations = Annotations.EMPTY,
        name = Name.identifier(SavedStateResolveExtension.DEFAULT_NAME),
        outType = type.getTypeIfTypeFromLiveData().makeNullable(),
        declaresDefaultValue = true,
        isCrossinline = false,
        isNoinline = false,
        varargElementType = null,
        source = source,
    )
}

private fun KotlinType.assertClassId(classId: ClassId): Boolean {
    return constructor.declarationDescriptor?.classId == classId
}

private val ClassDescriptor.mutableLifecycleClassDescriptor: ClassDescriptor
    get() = module.findClassAcrossModuleDependencies(MUTABLE_LIVE_DATA_CLASS)
        ?: error("Not found MutableLiveData class descriptor")


data class PropertyDescriptionData(
    val creations: List<Name>,
    val property: PropertyDescriptor
)
