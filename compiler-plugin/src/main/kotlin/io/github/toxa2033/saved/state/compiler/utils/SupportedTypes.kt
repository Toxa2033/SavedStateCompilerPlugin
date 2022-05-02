package io.github.toxa2033.saved.state.compiler.utils

import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

val supportedType = listOf(
    ClassId(FqName("kotlin"), Name.identifier("Double")),
    ClassId(FqName("kotlin"), Name.identifier("Int")),
    ClassId(FqName("kotlin"), Name.identifier("Long")),
    ClassId(FqName("kotlin"), Name.identifier("String")),
    ClassId(FqName("kotlin"), Name.identifier("Byte")),
    ClassId(FqName("kotlin"), Name.identifier("Char")),
    ClassId(FqName("kotlin"), Name.identifier("CharSequence")),
    ClassId(FqName("kotlin"), Name.identifier("Float")),
    ClassId(FqName("kotlin"), Name.identifier("Short")),
    ClassId(FqName("android.util"), Name.identifier("SparseArray")),
    ClassId(FqName("android.os"), Name.identifier("Binder")),
    ClassId(FqName("android.os"), Name.identifier("Bundle")),
    ClassId(FqName("kotlin.collections"), Name.identifier("ArrayList")),
    ClassId(FqName("kotlin.collections"), Name.identifier("List")),
    ClassId(FqName("java.util"), Name.identifier("ArrayList")),
    ClassId(FqName("kotlin"), Name.identifier("IntArray")),
    ClassId(FqName("kotlin"), Name.identifier("DoubleArray")),
    ClassId(FqName("kotlin"), Name.identifier("LongArray")),
    ClassId(FqName("kotlin"), Name.identifier("ByteArray")),
    ClassId(FqName("kotlin"), Name.identifier("CharArray")),
    ClassId(FqName("kotlin"), Name.identifier("FloatArray")),
    ClassId(FqName("kotlin"), Name.identifier("ShortArray")),
    ClassId(FqName("android.util"), Name.identifier("Size")),
    ClassId(FqName("android.util"), Name.identifier("SizeF"))
)

val supportedDeserializeInterface = listOf(
    ClassId(FqName("android.os"), Name.identifier("Parcelable")),
    ClassId(FqName("java.io"), Name.identifier("Serializable"))
)