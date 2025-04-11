package top.mcfpp.nbt.tags.collection

import org.apache.commons.lang3.ArrayUtils
import top.mcfpp.nbt.tags.primitive.LongTag
import top.mcfpp.nbt.visitors.StringTagVisitor
import top.mcfpp.nbt.visitors.TagVisitor
import java.util.*

class LongArrayTag(var asLongArray: LongArray) : CollectionTag<LongTag> {
    override fun toString(): String {
        val stringTagVisitor = StringTagVisitor()
        stringTagVisitor.visitLongArray(this)
        return stringTagVisitor.build()
    }

    override fun copy(): LongArrayTag {
        val ls = LongArray(asLongArray.size)
        System.arraycopy(this.asLongArray, 0, ls, 0, asLongArray.size)
        return LongArrayTag(ls)
    }

    override fun equals(`object`: Any?): Boolean {
        return this === `object` || `object` is LongArrayTag && asLongArray.contentEquals(`object`.asLongArray)
    }

    override fun hashCode(): Int {
        return asLongArray.contentHashCode()
    }

    override fun accept(tagVisitor: TagVisitor) {
        tagVisitor.visitLongArray(this)
    }

    override val size: Int  get() {
        return asLongArray.size
    }

    override operator fun get(index: Int): LongTag {
        return LongTag.valueOf(asLongArray[index])
    }

    override operator fun set(index: Int, tag: LongTag): LongTag {
        val l = asLongArray[index]
        asLongArray[index] = tag.longValue()
        return LongTag.valueOf(l)
    }

    override fun add(index: Int, tag: LongTag) {
        this.asLongArray = ArrayUtils.insert(index, this.asLongArray, tag.longValue())
    }

    override fun removeAt(index: Int): LongTag {
        val l = asLongArray[index]
        this.asLongArray = ArrayUtils.remove(this.asLongArray, index)
        return LongTag.valueOf(l)
    }

    override fun clear() {
        this.asLongArray = LongArray(0)
    }

    override fun asLongArray(): Optional<LongArray> {
        return Optional.of(this.asLongArray)
    }
}
