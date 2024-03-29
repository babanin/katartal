@file:Suppress("ClassName")

package katartal.model.attribute

import katartal.model.CPoolIndex
import katartal.util.DynamicByteArray

/**
 * StackMapTable_attribute {
 *     u2              attribute_name_index;
 *     u4              attribute_length;
 *     u2              number_of_entries;
 *     stack_map_frame entries[number_of_entries];
 * }
 */
class StackMapTableAttribute(attributeNameIndex: CPoolIndex, private val frames: List<StackMapFrameAttribute>) :
    Attribute(attributeNameIndex), MethodCodeAttribute {
    override fun generateAttributeData(): ByteArray {
        val localVarAttributeArray = DynamicByteArray()
        localVarAttributeArray.putU2(frames.size)

        for (frame in frames) {
            localVarAttributeArray.putByteArray(frame.toByteArray())
        }

        return localVarAttributeArray.toByteArray()
    }
}

/**
 * union stack_map_frame {
 *     same_frame;
 *     same_locals_1_stack_item_frame;
 *     same_locals_1_stack_item_frame_extended;
 *     chop_frame;
 *     same_frame_extended;
 *     append_frame;
 *     full_frame;
 * }
 */
sealed class StackMapFrameAttribute(val frameType: UByte) {
    fun toByteArray(): ByteArray {
        val dynamicByteArray = DynamicByteArray()
        dynamicByteArray.putU1(frameType)

        writeCustomData(dynamicByteArray)

        return dynamicByteArray.toByteArray()
    }

    open fun writeCustomData(dynamicByteArray: DynamicByteArray) = Unit

    override fun toString(): String = "${this.javaClass.simpleName} frameType=${frameType}"
}

class same_frame(frameType: UByte) : StackMapFrameAttribute(frameType) {
    init {
        if (frameType > 63u) {
            throw IllegalStateException(
                "Invalid frame type: ${frameType}. " +
                        "same_frame is represented by tags in the range [0-63]."
            )
        }
    }
}

class same_locals_1_stack_item_frame(frameType: UByte, private val verificationTypeInfo: VerificationTypeInfo) :
    StackMapFrameAttribute(frameType) {
    init {
        if (frameType < 64u || frameType > 127u) {
            throw IllegalStateException(
                "Invalid frame type: ${frameType}. " +
                        "same_locals_1_stack_item_frame is represented by tags in the range [64, 127]"
            )
        }
    }

    override fun writeCustomData(dynamicByteArray: DynamicByteArray) {
        dynamicByteArray.putU1(verificationTypeInfo.tag.code)
    }
}

class same_locals_1_stack_item_frame_extended(
    private val offsetDelta: UShort,
    private val verificationTypeInfo: VerificationTypeInfo
) :
    StackMapFrameAttribute(247u) {
    override fun writeCustomData(dynamicByteArray: DynamicByteArray) {
        dynamicByteArray.putU2(offsetDelta)
        dynamicByteArray.putU1(verificationTypeInfo.tag.code)
    }
}

class chop_frame(private val offsetDelta: UShort, k: UByte) : StackMapFrameAttribute((251u - k).toUByte()) {
    init {
        if (frameType < 248u || frameType > 250u) {
            throw IllegalStateException(
                "Invalid frame type: ${frameType}. " +
                        "chop_frame is represented by tags in the range [248-250]"
            )
        }
    }

    override fun writeCustomData(dynamicByteArray: DynamicByteArray) {
        dynamicByteArray.putU2(offsetDelta)
    }

    override fun toString(): String = "chop_frame offset=${offsetDelta}"
}

class same_frame_extended(private val offsetDelta: UShort) : StackMapFrameAttribute(251u) {
    override fun writeCustomData(dynamicByteArray: DynamicByteArray) {
        dynamicByteArray.putU2(offsetDelta)
    }
}

class append_frame(
    private val offsetDelta: UShort,
    private val locals: List<VerificationTypeInfo>
) :
    StackMapFrameAttribute((251 + locals.size).toUByte()) {

    init {
        if (frameType < 252u || frameType > 254u) {
            throw IllegalStateException(
                "Invalid frame type: ${frameType}. " +
                        "append_frame is represented by tags in the range [252-254]"
            )
        }
    }

    override fun writeCustomData(dynamicByteArray: DynamicByteArray) {
        dynamicByteArray.putU2(offsetDelta)

        for (local in locals) {
            dynamicByteArray.putByteArray(local.toByteArray())
        }
    }

    override fun toString(): String = "append_frame offset=${offsetDelta} locals=${locals}"
}

class full_frame(
    private val offsetDelta: UShort,
    private val locals: List<VerificationTypeInfo>,
    private val stacks: List<VerificationTypeInfo>
) : StackMapFrameAttribute(255u) {
    override fun writeCustomData(dynamicByteArray: DynamicByteArray) {
        dynamicByteArray.putU2(offsetDelta)

        dynamicByteArray.putU2(locals.size)
        for (local in locals) {
            dynamicByteArray.putByteArray(local.toByteArray())
        }

        dynamicByteArray.putU2(stacks.size)
        for (stack in stacks) {
            dynamicByteArray.putByteArray(stack.toByteArray())
        }
    }
}

enum class VerificationTypeTag(val code: UByte) {
    ITEM_Top(0u),
    ITEM_Integer(1u),
    ITEM_Float(2u),
    ITEM_Double(3u),
    ITEM_Long(4u),
    ITEM_Null(5u),
    ITEM_UninitializedThis(6u),
    ITEM_Object(7u),
    ITEM_Uninitialized(8u)
}

/**
 * union verification_type_info {
 *     Top_variable_info;
 *     Integer_variable_info;
 *     Float_variable_info;
 *     Long_variable_info;
 *     Double_variable_info;
 *     Null_variable_info;
 *     UninitializedThis_variable_info;
 *     Object_variable_info;
 *     Uninitialized_variable_info;
 * }
 */
sealed class VerificationTypeInfo(val tag: VerificationTypeTag) {
    open fun toByteArray(): ByteArray {
        return byteArrayOf(tag.code.toByte())
    }

    override fun toString(): String = "${this.javaClass.simpleName}(tag=$tag)"
}

/**
 * Top_variable_info {
 *     u1 tag = ITEM_Top; /* 0 */
 * }
 */
class Top_variable_info : VerificationTypeInfo(VerificationTypeTag.ITEM_Top)

/**
 * Integer_variable_info {
 *     u1 tag = ITEM_Integer; /* 1 */
 * }
 */
class Integer_variable_info : VerificationTypeInfo(VerificationTypeTag.ITEM_Integer)

/**
 * Float_variable_info {
 *     u1 tag = ITEM_Float; /* 2 */
 * }
 */
class Float_variable_info : VerificationTypeInfo(VerificationTypeTag.ITEM_Float)

/**
 * Double_variable_info {
 *     u1 tag = ITEM_Double; /* 3 */
 * }
 */
class Double_variable_info : VerificationTypeInfo(VerificationTypeTag.ITEM_Double)

/**
 * Long_variable_info {
 *     u1 tag = ITEM_Long; /* 4 */
 * }
 */
class Long_variable_info : VerificationTypeInfo(VerificationTypeTag.ITEM_Long)

/**
 * Null_variable_info {
 *     u1 tag = ITEM_Null; /* 5 */
 * }
 */
class Null_variable_info : VerificationTypeInfo(VerificationTypeTag.ITEM_Null)

/**
 * UninitializedThis_variable_info {
 *     u1 tag = ITEM_UninitializedThis; /* 6 */
 * }
 */
class UninitializedThis_variable_info : VerificationTypeInfo(VerificationTypeTag.ITEM_UninitializedThis)

/**
 * Uninitialized_variable_info {
 *     u1 tag = ITEM_Uninitialized; /* 8 */
 *     u2 offset;
 * }
 */
class Uninitialized_variable_info(private val offset: UShort) :
    VerificationTypeInfo(VerificationTypeTag.ITEM_Uninitialized) {
    override fun toByteArray(): ByteArray {
        return DynamicByteArray(3).apply {
            putU1(tag.code)
            putU2(offset)
        }.toByteArray()
    }

    override fun toString(): String = "${this.javaClass.simpleName}(tag=$tag, offset=$offset)"
}

/**
 * Object_variable_info {
 *     u1 tag = ITEM_Object; /* 7 */
 *     u2 cpool_index;
 * }
 */
class Object_variable_info(private val cPoolIndex: CPoolIndex) : VerificationTypeInfo(VerificationTypeTag.ITEM_Object) {
    override fun toByteArray(): ByteArray {
        return DynamicByteArray(3).apply {
            putU1(tag.code)
            putU2(cPoolIndex.index)
        }.toByteArray()
    }

    override fun toString(): String = "${this.javaClass.simpleName}(tag=$tag, cPoolIndex=$cPoolIndex)"
}