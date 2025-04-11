package top.mcfpp.nbt.tags.collection

import org.apache.commons.lang3.ArrayUtils
import top.mcfpp.nbt.tags.Tag
import top.mcfpp.nbt.tags.primitive.ByteTag
import top.mcfpp.nbt.visitors.StringTagVisitor
import top.mcfpp.nbt.visitors.TagVisitor
import java.util.*

class ByteArrayTag(var asByteArray: ByteArray) : CollectionTag<ByteTag> {
    override fun toString(): String {
        val stringTagVisitor = StringTagVisitor()
        stringTagVisitor.visitByteArray(this)
        return stringTagVisitor.build()
    }

    override fun copy(): Tag {
        val bs = ByteArray(asByteArray.size)
        System.arraycopy(this.asByteArray, 0, bs, 0, asByteArray.size)
        return ByteArrayTag(bs)
    }

    override fun equals(`object`: Any?): Boolean {
        return this === `object` || `object` is ByteArrayTag && asByteArray.contentEquals(`object`.asByteArray)
    }

    override fun hashCode(): Int {
        return asByteArray.contentHashCode()
    }

    override fun accept(tagVisitor: TagVisitor) {
        tagVisitor.visitByteArray(this)
    }

    override val size: Int get() {
        return asByteArray.size
    }

    override operator fun get(index: Int): ByteTag {
        return ByteTag.valueOf(asByteArray[index])
    }

    override operator fun set(index: Int, tag: ByteTag): ByteTag {
        val b = asByteArray[index]
        asByteArray[index] = tag.byteValue()
        return ByteTag.valueOf(b)
    }

    override fun add(index: Int, tag: ByteTag) {
        this.asByteArray = ArrayUtils.insert(index, this.asByteArray, tag.byteValue())
    }

    override fun removeAt(index: Int): ByteTag {
        val b = asByteArray[index]
        this.asByteArray = ArrayUtils.remove(this.asByteArray, index)
        return ByteTag.valueOf(b)
    }

    override fun clear() {
        this.asByteArray = ByteArray(0)
    }

    override fun asByteArray(): Optional<ByteArray> {
        return Optional.of(this.asByteArray)
    }
}
