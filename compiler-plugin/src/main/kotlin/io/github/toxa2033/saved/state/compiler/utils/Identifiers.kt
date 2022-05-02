package io.github.toxa2033.saved.state.compiler.utils

import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

object FqNames {
    val SAVE_STATE_ANNOTATION_NAME = FqName("io.github.toxa2033.saved.state.core.SaveState")
}

const val LIVE_DATA = "LiveData"

val LIVE_DATA_CLASS = ClassId(
    FqName("androidx.lifecycle"),
    Name.identifier("LiveData")
)
val MUTABLE_LIVE_DATA_CLASS = ClassId(
    FqName("androidx.lifecycle"),
    Name.identifier("MutableLiveData")
)

val TYPE_STATE_HANDLER = ClassId(
    FqName("androidx.lifecycle"),
    Name.identifier("SavedStateHandle")
)

const val IDENTIFIER = "_IDENTIFIER"


const val UNSUPPORTED_TYPE_ERROR = "This type is not supported for save in SavedStateHandle" +
        "\nFor more information check documentation: " +
        "https://developer.android.com/topic/libraries/architecture/viewmodel-savedstate#types"

const val NOT_FOUND_SAVED_STATE_HANDLER_PROPERTY_ERROR = "androidx.lifecycle.SavedStateHandle must be declared in class"