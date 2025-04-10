package top.mcfpp.nbt.visitors

import top.mcfpp.nbt.tags.CompoundTag
import top.mcfpp.nbt.tags.EndTag
import top.mcfpp.nbt.tags.collection.ByteArrayTag
import top.mcfpp.nbt.tags.collection.IntArrayTag
import top.mcfpp.nbt.tags.collection.ListTag
import top.mcfpp.nbt.tags.collection.LongArrayTag
import top.mcfpp.nbt.tags.primitive.*

interface TagVisitor {
    fun visitString(stringTag: StringTag)

    fun visitByte(byteTag: ByteTag)

    fun visitShort(shortTag: ShortTag)

    fun visitInt(intTag: IntTag)

    fun visitLong(longTag: LongTag)

    fun visitFloat(floatTag: FloatTag)

    fun visitDouble(doubleTag: DoubleTag)

    fun visitByteArray(byteArrayTag: ByteArrayTag)

    fun visitIntArray(intArrayTag: IntArrayTag)

    fun visitLongArray(longArrayTag: LongArrayTag)

    fun visitList(listTag: ListTag)

    fun visitCompound(compoundTag: CompoundTag)

    fun visitEnd(endTag: EndTag)
}
