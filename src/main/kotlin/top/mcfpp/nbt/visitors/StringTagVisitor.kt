package top.mcfpp.nbt.visitors

import top.mcfpp.nbt.tags.CompoundTag
import top.mcfpp.nbt.tags.EndTag
import top.mcfpp.nbt.tags.Tag
import top.mcfpp.nbt.tags.collection.ByteArrayTag
import top.mcfpp.nbt.tags.collection.IntArrayTag
import top.mcfpp.nbt.tags.collection.ListTag
import top.mcfpp.nbt.tags.collection.LongArrayTag
import top.mcfpp.nbt.tags.primitive.*
import java.util.Map.Entry.comparingByKey
import java.util.regex.Pattern

class StringTagVisitor : TagVisitor {
    private val builder = StringBuilder()

    fun build(): String {
        return builder.toString()
    }

    override fun visitString(stringTag: StringTag) {
        builder.append(StringTag.quoteAndEscape(stringTag.value))
    }

    override fun visitByte(byteTag: ByteTag) {
        builder.append(byteTag.value.toInt()).append('b')
    }

    override fun visitShort(shortTag: ShortTag) {
        builder.append(shortTag.value.toInt()).append('s')
    }

    override fun visitInt(intTag: IntTag) {
        builder.append(intTag.value)
    }

    override fun visitLong(longTag: LongTag) {
        builder.append(longTag.value).append('L')
    }

    override fun visitFloat(floatTag: FloatTag) {
        builder.append(floatTag.value).append('f')
    }

    override fun visitDouble(doubleTag: DoubleTag) {
        builder.append(doubleTag.value).append('d')
    }

    override fun visitByteArray(byteArrayTag: ByteArrayTag) {
        builder.append("[B;")
        val bs = byteArrayTag.asByteArray

        for (i in bs.indices) {
            if (i != 0) {
                builder.append(',')
            }

            builder.append(bs[i].toInt()).append('B')
        }

        builder.append(']')
    }

    override fun visitIntArray(intArrayTag: IntArrayTag) {
        builder.append("[I;")
        val `is` = intArrayTag.asIntArray

        for (i in `is`.indices) {
            if (i != 0) {
                builder.append(',')
            }

            builder.append(`is`[i])
        }

        builder.append(']')
    }

    override fun visitLongArray(longArrayTag: LongArrayTag) {
        builder.append("[L;")
        val ls = longArrayTag.asLongArray

        for (i in ls.indices) {
            if (i != 0) {
                builder.append(',')
            }

            builder.append(ls[i]).append('L')
        }

        builder.append(']')
    }

    override fun visitList(listTag: ListTag) {
        builder.append('[')

        for (i in listTag.indices) {
            if (i != 0) {
                builder.append(',')
            }

            listTag[i].accept(this)
        }

        builder.append(']')
    }

    override fun visitCompound(compoundTag: CompoundTag) {
        builder.append('{')
        val list: MutableList<Map.Entry<String, Tag>> = ArrayList(compoundTag.entrySet())
        list.sortWith(comparingByKey())

        for (i in list.indices) {
            val entry = list[i]
            if (i != 0) {
                builder.append(',')
            }

            this.handleKeyEscape(entry.key)
            builder.append(':')
            entry.value.accept(this)
        }

        builder.append('}')
    }

    private fun handleKeyEscape(string: String) {
        if (!string.equals("true", ignoreCase = true) && !string.equals(
                "false",
                ignoreCase = true
            ) && UNQUOTED_KEY_MATCH.matcher(string).matches()
        ) {
            builder.append(string)
        } else {
            StringTag.quoteAndEscape(string, this.builder)
        }
    }

    override fun visitEnd(endTag: EndTag) {
        builder.append("END")
    }

    companion object {
        private val UNQUOTED_KEY_MATCH: Pattern = Pattern.compile("[A-Za-z._]+[A-Za-z0-9._+-]*")
    }
}
