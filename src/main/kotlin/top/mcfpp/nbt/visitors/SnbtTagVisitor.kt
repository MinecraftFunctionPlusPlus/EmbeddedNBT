package top.mcfpp.nbt.visitors

import top.mcfpp.nbt.tags.CompoundTag
import top.mcfpp.nbt.tags.EndTag
import top.mcfpp.nbt.tags.Tag
import top.mcfpp.nbt.tags.collection.ByteArrayTag
import top.mcfpp.nbt.tags.collection.IntArrayTag
import top.mcfpp.nbt.tags.collection.ListTag
import top.mcfpp.nbt.tags.collection.LongArrayTag
import top.mcfpp.nbt.tags.primitive.*

class SnbtTagVisitor: TagVisitor {

    private var result = ""

    fun visit(tag: Tag?): String {
        tag!!.accept(this)
        return this.result
    }

    override fun visitString(stringTag: StringTag) {
        this.result = StringTag.quoteAndEscape(stringTag.value)
    }

    override fun visitByte(byteTag: ByteTag) {
        this.result = byteTag.value.toString() + "b"
    }

    override fun visitShort(shortTag: ShortTag) {
        this.result = shortTag.value.toString() + "s"
    }

    override fun visitInt(intTag: IntTag) {
        this.result = intTag.value.toString()
    }

    override fun visitLong(longTag: LongTag) {
        this.result = longTag.value.toString() + "L"
    }

    override fun visitFloat(floatTag: FloatTag) {
        this.result = floatTag.value.toString() + "f"
    }

    override fun visitDouble(doubleTag: DoubleTag) {
        this.result = doubleTag.value.toString() + "d"
    }

    override fun visitByteArray(byteArrayTag: ByteArrayTag) {
        val stringBuilder = StringBuilder(LIST_OPEN).append("B").append(LIST_TYPE_SEPARATOR)
        val bs = byteArrayTag.asByteArray

        for (i in bs.indices) {
            stringBuilder.append(ELEMENT_SPACING).append(bs[i].toInt()).append("B")
            if (i != bs.size - 1) {
                stringBuilder.append(ELEMENT_SEPARATOR)
            }
        }

        stringBuilder.append(LIST_CLOSE)
        this.result = stringBuilder.toString()
    }

    override fun visitIntArray(intArrayTag: IntArrayTag) {
        val stringBuilder = StringBuilder(LIST_OPEN).append("I").append(LIST_TYPE_SEPARATOR)
        val `is` = intArrayTag.asIntArray

        for (i in `is`.indices) {
            stringBuilder.append(ELEMENT_SPACING).append(`is`[i])
            if (i != `is`.size - 1) {
                stringBuilder.append(ELEMENT_SEPARATOR)
            }
        }

        stringBuilder.append(LIST_CLOSE)
        this.result = stringBuilder.toString()
    }

    override fun visitLongArray(longArrayTag: LongArrayTag) {
        val stringBuilder = StringBuilder(LIST_OPEN).append("L").append(LIST_TYPE_SEPARATOR)
        val ls = longArrayTag.asLongArray

        for (i in ls.indices) {
            stringBuilder.append(ELEMENT_SPACING).append(ls[i]).append("L")
            if (i != ls.size - 1) {
                stringBuilder.append(ELEMENT_SEPARATOR)
            }
        }

        stringBuilder.append(LIST_CLOSE)
        this.result = stringBuilder.toString()
    }

    override fun visitList(listTag: ListTag) {
        val warpedListTag = listTag.wrappedList
        if (warpedListTag.isEmpty) {
            this.result = "[]"
        } else {
            val stringBuilder = StringBuilder(LIST_OPEN)

            for (i in warpedListTag.indices) {
                stringBuilder.append(SnbtTagVisitor().visit(warpedListTag[i]))
                if (i != warpedListTag.size - 1) {
                    stringBuilder.append(ELEMENT_SEPARATOR)
                }
            }
            stringBuilder.append(LIST_CLOSE)
            this.result = stringBuilder.toString()
        }
    }

    override fun visitCompound(compoundTag: CompoundTag) {
        if (compoundTag.isEmpty) {
            this.result = "{}"
        } else {
            val stringBuilder = StringBuilder(STRUCT_OPEN)

            val iterator = compoundTag.keySet().iterator()

            while (iterator.hasNext()) {
                val key = iterator.next()
                val tag = compoundTag[key]
                stringBuilder
                    .append(key)
                    .append(NAME_VALUE_SEPARATOR)
                    .append(SnbtTagVisitor().visit(tag))
                if (iterator.hasNext()) {
                    stringBuilder.append(ELEMENT_SEPARATOR)
                }
            }

            stringBuilder.append(STRUCT_CLOSE)
            this.result = stringBuilder.toString()
        }
    }

    override fun visitEnd(endTag: EndTag) {}


    companion object {
        private const val NAME_VALUE_SEPARATOR = ":"
        private const val ELEMENT_SEPARATOR = ","
        private const val LIST_OPEN = "["
        private const val LIST_CLOSE = "]"
        private const val LIST_TYPE_SEPARATOR = ";"
        private const val ELEMENT_SPACING = " "
        private const val STRUCT_OPEN = "{"
        private const val STRUCT_CLOSE = "}"
        private const val NEWLINE = "\n"
    }
}