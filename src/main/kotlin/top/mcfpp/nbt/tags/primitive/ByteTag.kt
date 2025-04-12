package top.mcfpp.nbt.tags.primitive

import top.mcfpp.nbt.visitors.StringTagVisitor
import top.mcfpp.nbt.visitors.TagVisitor

@JvmRecord
data class ByteTag(override val value: Byte) : NumericTag<Byte> {

    constructor() : this(0.toByte())

    constructor(value: Boolean) : this(if(value) 1.toByte() else 0.toByte())

    override fun copy(): ByteTag {
        return this
    }

    override fun accept(tagVisitor: TagVisitor) {
        tagVisitor.visitByte(this)
    }

    override fun longValue(): Long {
        return value.toLong()
    }

    override fun intValue(): Int {
        return value.toInt()
    }

    override fun shortValue(): Short {
        return value.toShort()
    }

    override fun byteValue(): Byte {
        return this.value
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
        stringTagVisitor.visitByte(this)
        return stringTagVisitor.build()
    }

    internal object Cache {
        val cache: Array<ByteTag> = Array(256){
            ByteTag((it - 128).toByte())
        }
    }

    companion object {
        val ZERO: ByteTag = valueOf(0.toByte())
        val ONE: ByteTag = valueOf(1.toByte())

        @JvmStatic
		fun valueOf(b: Byte): ByteTag {
            return Cache.cache[128 + b]
        }

        @JvmStatic
		fun valueOf(bl: Boolean): ByteTag {
            return if (bl) ONE else ZERO
        }
    }
}
