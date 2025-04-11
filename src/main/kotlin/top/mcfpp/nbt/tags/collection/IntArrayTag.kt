package top.mcfpp.nbt.tags.collection

import org.apache.commons.lang3.ArrayUtils
import top.mcfpp.nbt.tags.primitive.IntTag
import top.mcfpp.nbt.visitors.StringTagVisitor
import top.mcfpp.nbt.visitors.TagVisitor
import java.util.*

class IntArrayTag(var asIntArray: IntArray) : CollectionTag<IntTag> {
    override fun toString(): String {
        val stringTagVisitor = StringTagVisitor()
        stringTagVisitor.visitIntArray(this)
        return stringTagVisitor.build()
    }

    override fun copy(): IntArrayTag {
        val `is` = IntArray(asIntArray.size)
        System.arraycopy(this.asIntArray, 0, `is`, 0, asIntArray.size)
        return IntArrayTag(`is`)
    }

    override fun equals(`object`: Any?): Boolean {
        return this === `object` || `object` is IntArrayTag && asIntArray.contentEquals(`object`.asIntArray)
    }

    override fun hashCode(): Int {
        return asIntArray.contentHashCode()
    }

    override fun accept(tagVisitor: TagVisitor) {
        tagVisitor.visitIntArray(this)
    }

    override val size:Int get() {
        return asIntArray.size
    }

    override operator fun get(index: Int): IntTag {
        return IntTag.valueOf(asIntArray[index])
    }

    override operator fun set(index: Int, tag: IntTag): IntTag {
        val j = asIntArray[index]
        asIntArray[index] = tag.intValue()
        return IntTag.valueOf(j)
    }

    override fun add(index: Int, tag: IntTag) {
        this.asIntArray = ArrayUtils.insert(index, this.asIntArray, tag.intValue())
    }

    override fun removeAt(index: Int): IntTag {
        val j = asIntArray[index]
        this.asIntArray = ArrayUtils.remove(this.asIntArray, index)
        return IntTag.valueOf(j)
    }

    override fun clear() {
        this.asIntArray = IntArray(0)
    }

    override fun asIntArray(): Optional<IntArray> {
        return Optional.of(this.asIntArray)
    }
}
