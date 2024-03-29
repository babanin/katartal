package katartal.model.attribute

import katartal.model.CPoolIndex
import katartal.util.DynamicByteArray

sealed class Attribute(private val attributeNameIndex: CPoolIndex) {
    fun toByteArray(): ByteArray {
        val attributeData = generateAttributeData()

        val result = DynamicByteArray()
        result.putU2(attributeNameIndex.toUInt())
        result.putU4(attributeData.size)
        result.putByteArray(attributeData)
        return result.toByteArray()
    }

    abstract fun generateAttributeData(): ByteArray
}

interface ClassAttribute {
    fun toByteArray(): ByteArray
}

interface MethodAttribute {
    fun toByteArray(): ByteArray
}

interface MethodCodeAttribute {
    fun toByteArray(): ByteArray
}

interface FieldAttribute {
    fun toByteArray(): ByteArray
}

interface RecordComponentAttribute {
    fun toByteArray(): ByteArray
}