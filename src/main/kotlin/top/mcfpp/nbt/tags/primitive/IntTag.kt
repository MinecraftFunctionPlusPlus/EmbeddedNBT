package top.mcfpp.nbt.tags.primitive

import top.mcfpp.nbt.visitors.StringTagVisitor
import top.mcfpp.nbt.visitors.TagVisitor

@JvmRecord
data class IntTag(override val value: Int) : NumericTag<Int> {
    override fun copy(): IntTag {
        return this
    }

    override fun accept(tagVisitor: TagVisitor) {
        tagVisitor.visitInt(this)
    }

    override fun longValue(): Long {
        return value.toLong()
    }

    override fun intValue(): Int {
        return this.value
    }

    override fun shortValue(): Short {
        return (this.value and 65535).toShort()
    }

    override fun byteValue(): Byte {
        return (this.value and 0xFF).toByte()
    }

    override fun doubleValue(): Double {
        return value.toDouble()
    }

    override fun floatValue(): Float {
        return value.toFloat()
    }

    override fun box(): Number {
        return this.value
    }

    override fun toString(): String {
        val stringTagVisitor = StringTagVisitor()
        stringTagVisitor.visitInt(this)
        return stringTagVisitor.build()
    }

    internal object Cache {
        const val HIGH: Int = 1024
        const val LOW: Int = -128
        val cache: Array<IntTag> = Array(1153){
            IntTag(LOW + it)
        }
    }

    companion object {
        @JvmStatic
		fun valueOf(i: Int): IntTag {
            return if (i >= Cache.LOW && i <= Cache.HIGH) Cache.cache[i - Cache.LOW] else IntTag(i)
        }
    }
}
