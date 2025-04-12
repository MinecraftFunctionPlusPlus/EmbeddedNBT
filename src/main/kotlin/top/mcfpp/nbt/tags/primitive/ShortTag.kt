package top.mcfpp.nbt.tags.primitive

import top.mcfpp.nbt.visitors.StringTagVisitor
import top.mcfpp.nbt.visitors.TagVisitor

@JvmRecord
data class ShortTag(override val value: Short) : NumericTag<Short> {

    constructor() : this(0.toShort())

    override fun copy(): ShortTag {
        return this
    }

    override fun accept(tagVisitor: TagVisitor) {
        tagVisitor.visitShort(this)
    }

    override fun longValue(): Long {
        return value.toLong()
    }

    override fun intValue(): Int {
        return value.toInt()
    }

    override fun shortValue(): Short {
        return this.value
    }

    override fun byteValue(): Byte {
        return (value.toInt() and 255).toByte()
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
        stringTagVisitor.visitShort(this)
        return stringTagVisitor.build()
    }

    internal object Cache {
        const val HIGH: Int = 1024
        const val LOW: Int = -128
        val cache: Array<ShortTag> = Array(1153){
            ShortTag((LOW + it).toShort())
        }
    }

    companion object {
        @JvmStatic
		fun valueOf(s: Short): ShortTag {
            return if (s >= Cache.LOW && s <= Cache.HIGH) Cache.cache[s - Cache.LOW] else ShortTag(s)
        }
    }
}
