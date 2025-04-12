package top.mcfpp.nbt.tags.primitive

import top.mcfpp.nbt.visitors.StringTagVisitor
import top.mcfpp.nbt.visitors.TagVisitor

@JvmRecord
data class LongTag(override val value: Long) : NumericTag<Long> {

    constructor() : this(0L)
    override fun copy(): LongTag {
        return this
    }

    override fun accept(tagVisitor: TagVisitor) {
        tagVisitor.visitLong(this)
    }

    override fun longValue(): Long {
        return this.value
    }

    override fun intValue(): Int {
        return (this.value and -1L).toInt()
    }

    override fun shortValue(): Short {
        return (this.value and 65535L).toShort()
    }

    override fun byteValue(): Byte {
        return (this.value and 255L).toByte()
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
        stringTagVisitor.visitLong(this)
        return stringTagVisitor.build()
    }

    internal object Cache {
        const val HIGH: Int = 1024
        const val LOW: Int = -128
        val cache: Array<LongTag> = Array(1153){
            LongTag((LOW + it).toLong())
        }
    }

    companion object {
        @JvmStatic
		fun valueOf(l: Long): LongTag {
            return if (l >= Cache.LOW && l <= Cache.HIGH) Cache.cache[l.toInt() - Cache.LOW] else LongTag(l)
        }
    }
}
