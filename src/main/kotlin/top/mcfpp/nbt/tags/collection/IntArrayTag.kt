package top.mcfpp.nbt.tags.collection

import org.apache.commons.lang3.ArrayUtils
import top.mcfpp.nbt.tags.primitive.IntTag
import top.mcfpp.nbt.visitors.StringTagVisitor
import top.mcfpp.nbt.visitors.TagVisitor

class IntArrayTag(override var value: IntArray) : CollectionTag<IntTag, IntArray> {

    constructor() : this(IntArray(0))

    override fun toString(): String {
        val stringTagVisitor = StringTagVisitor()
        stringTagVisitor.visitIntArray(this)
        return stringTagVisitor.build()
    }

    override fun copy(): IntArrayTag {
        val `is` = IntArray(value.size)
        System.arraycopy(this.value, 0, `is`, 0, value.size)
        return IntArrayTag(`is`)
    }

    override fun equals(other: Any?): Boolean {
        return this === other || other is IntArrayTag && value.contentEquals(other.value)
    }

    override fun hashCode(): Int {
        return value.contentHashCode()
    }

    override fun accept(tagVisitor: TagVisitor) {
        tagVisitor.visitIntArray(this)
    }

    override val size:Int get() {
        return value.size
    }

    override operator fun get(index: Int): IntTag {
        return IntTag.valueOf(value[index])
    }

    override operator fun set(index: Int, tag: IntTag): IntTag {
        val j = value[index]
        value[index] = tag.intValue()
        return IntTag.valueOf(j)
    }

    override fun add(index: Int, tag: IntTag) {
        this.value = ArrayUtils.insert(index, this.value, tag.intValue())
    }

    override fun removeAt(index: Int): IntTag {
        val j = value[index]
        this.value = ArrayUtils.remove(this.value, index)
        return IntTag.valueOf(j)
    }

    override fun clear() {
        this.value = IntArray(0)
    }

    override fun asIntArray(): IntArray {
        return this.value
    }
}
