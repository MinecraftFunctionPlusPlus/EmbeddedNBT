package top.mcfpp.nbt.tags.collection

import org.apache.commons.lang3.ArrayUtils
import top.mcfpp.nbt.tags.Tag
import top.mcfpp.nbt.tags.primitive.ByteTag
import top.mcfpp.nbt.visitors.StringTagVisitor
import top.mcfpp.nbt.visitors.TagVisitor
import java.util.*

class ByteArrayTag(override var value: ByteArray) : CollectionTag<ByteTag, ByteArray> {
    override fun toString(): String {
        val stringTagVisitor = StringTagVisitor()
        stringTagVisitor.visitByteArray(this)
        return stringTagVisitor.build()
    }

    override fun copy(): ByteArrayTag {
        val bs = ByteArray(value.size)
        System.arraycopy(this.value, 0, bs, 0, value.size)
        return ByteArrayTag(bs)
    }

    override fun equals(other: Any?): Boolean {
        return this === other || other is ByteArrayTag && value.contentEquals(other.value)
    }

    override fun hashCode(): Int {
        return value.contentHashCode()
    }

    override fun accept(tagVisitor: TagVisitor) {
        tagVisitor.visitByteArray(this)
    }

    override val size: Int get() {
        return value.size
    }

    override operator fun get(index: Int): ByteTag {
        return ByteTag.valueOf(value[index])
    }

    override operator fun set(index: Int, tag: ByteTag): ByteTag {
        val b = value[index]
        value[index] = tag.byteValue()
        return ByteTag.valueOf(b)
    }

    override fun add(index: Int, tag: ByteTag) {
        this.value = ArrayUtils.insert(index, this.value, tag.byteValue())
    }

    override fun removeAt(index: Int): ByteTag {
        val b = value[index]
        this.value = ArrayUtils.remove(this.value, index)
        return ByteTag.valueOf(b)
    }

    override fun clear() {
        this.value = ByteArray(0)
    }

    override fun asByteArray(): ByteArray {
        return this.value
    }
}
