package katartal.generators.plain

import katartal.generators.ClassGenerator
import katartal.model.ClassBuilder
import katartal.model.CodeAttribute
import katartal.util.DynamicByteArray

class PlainClassGenerator : ClassGenerator {

    /**
     * ClassFile {
     *     u4             magic;
     *     u2             minor_version;
     *     u2             major_version;
     *     u2             constant_pool_count;
     *     cp_info        constant_pool[constant_pool_count-1];
     *     u2             access_flags;
     *     u2             this_class;
     *     u2             super_class;
     *     u2             interfaces_count;
     *     u2             interfaces[interfaces_count];
     *     u2             fields_count;
     *     field_info     fields[fields_count];
     *     u2             methods_count;
     *     method_info    methods[methods_count];
     *     u2             attributes_count;
     *     attribute_info attributes[attributes_count];
     * }
     */
    override fun toByteArray(clsBuilder: ClassBuilder): ByteArray {
        val cls = DynamicByteArray()
        cls.putU4(0xCAFEBABEu)
        cls.putU2(0u) // minor version
        cls.putU2(clsBuilder.version.opcode) // major version

        cls.putU2((clsBuilder.constantPool.size + 1).toUInt()) // constant pool size
        for (entry in clsBuilder.constantPool) {
            cls.putByteArray(entry.toByteArray())
        }

        cls.putU2(clsBuilder.access.opcode) // access flags

        cls.putU2(clsBuilder.classNameIdx)// this class
        cls.putU2(clsBuilder.parentClassNameIdx) // super class

        cls.putU2(clsBuilder.implements.size.toUInt()) // interfaces_count
        for (interfaceIdx in clsBuilder.implements) { // interfaces
            cls.putU2(interfaceIdx) // super class
        }

        cls.putU2(0u) // fields_count
        // fields

        cls.putU2(clsBuilder.methodBuilders.size.toUInt()) // methods_count
        for (methodBuilder in clsBuilder.methodBuilders) {
            cls.putU2(methodBuilder.access.opcode)
            cls.putU2(methodBuilder.nameCpIndex)
            cls.putU2(methodBuilder.descriptorCpIndex)

            val attributes = methodBuilder.attributes
            cls.putU2(attributes.size)
            for (attribute in attributes) {
                cls.putByteArray(attribute.toByteArray())
            }
        }
        // methods

        cls.putU2(0u) // attributes_count
        // attributes

        return cls.toByteArray()
    }
}