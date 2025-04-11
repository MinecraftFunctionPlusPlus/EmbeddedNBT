package top.mcfpp.nbt.tags.primitive

import top.mcfpp.nbt.visitors.StringTagVisitor
import top.mcfpp.nbt.visitors.TagVisitor
import top.mcfpp.utils.Math.floor

@JvmRecord
data class DoubleTag(val value: Double) : NumericTag {
    override fun copy(): DoubleTag {
        return this
    }

    override fun accept(tagVisitor: TagVisitor) {
        tagVisitor.visitDouble(this)
    }

    override fun longValue(): Long {
        return kotlin.math.floor(this.value).toLong()
    }

    override fun intValue(): Int {
        return floor(this.value)
    }

    override fun shortValue(): Short {
        return (floor(this.value) and 65535).toShort()
    }

    override fun byteValue(): Byte {
        return (floor(this.value) and 0xFF).toByte()
    }

    override fun doubleValue(): Double {
        return this.value
    }

    override fun floatValue(): Float {
        return value.toFloat()
    }

    override fun box(): Number {
        return this.value
    }


    override fun toString(): String {
        val stringTagVisitor = StringTagVisitor()
        stringTagVisitor.visitDouble(this)
        return stringTagVisitor.build()
    }

    companion object {
        val ZERO: DoubleTag = DoubleTag(0.0)
        @JvmStatic
		fun valueOf(d: Double): DoubleTag {
            return if (d == 0.0) ZERO else DoubleTag(d)
        }
    }
}
