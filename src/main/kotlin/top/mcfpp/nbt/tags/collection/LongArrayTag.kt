package top.mcfpp.nbt.tags.collection

import org.apache.commons.lang3.ArrayUtils
import top.mcfpp.nbt.tags.primitive.LongTag
import top.mcfpp.nbt.visitors.StringTagVisitor
import top.mcfpp.nbt.visitors.TagVisitor

class LongArrayTag(override var value: LongArray) : CollectionTag<LongTag, LongArray> {

    constructor() : this(LongArray(0))

    override fun toString(): String {
        val stringTagVisitor = StringTagVisitor()
        stringTagVisitor.visitLongArray(this)
        return stringTagVisitor.build()
    }

    override fun copy(): LongArrayTag {
        val ls = LongArray(value.size)
        System.arraycopy(this.value, 0, ls, 0, value.size)
        return LongArrayTag(ls)
    }

    override fun equals(other: Any?): Boolean {
        return this === other || other is LongArrayTag && value.contentEquals(other.value)
    }

    override fun hashCode(): Int {
        return value.contentHashCode()
    }

    override fun accept(tagVisitor: TagVisitor) {
        tagVisitor.visitLongArray(this)
    }

    override val size: Int  get() {
        return value.size
    }

    override operator fun get(index: Int): LongTag {
        return LongTag.valueOf(value[index])
    }

    override operator fun set(index: Int, tag: LongTag): LongTag {
        val l = value[index]
        value[index] = tag.longValue()
        return LongTag.valueOf(l)
    }

    override fun add(index: Int, tag: LongTag) {
        this.value = ArrayUtils.insert(index, this.value, tag.longValue())
    }

    override fun removeAt(index: Int): LongTag {
        val l = value[index]
        this.value = ArrayUtils.remove(this.value, index)
        return LongTag.valueOf(l)
    }

    override fun clear() {
        this.value = LongArray(0)
    }

    override fun asLongArray(): LongArray {
        return this.value
    }
}
