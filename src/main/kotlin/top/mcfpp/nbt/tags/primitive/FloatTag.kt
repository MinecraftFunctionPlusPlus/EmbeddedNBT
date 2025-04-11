package top.mcfpp.nbt.tags.primitive

import top.mcfpp.nbt.visitors.StringTagVisitor
import top.mcfpp.nbt.visitors.TagVisitor
import top.mcfpp.utils.Math.floor

@JvmRecord
data class FloatTag(override val value: Float) : NumericTag<Float> {
    override fun copy(): FloatTag {
        return this
    }

    override fun accept(tagVisitor: TagVisitor) {
        tagVisitor.visitFloat(this)
    }

    override fun longValue(): Long {
        return value.toLong()
    }

    override fun intValue(): Int {
        return floor(value.toDouble())
    }

    override fun shortValue(): Short {
        return (floor(value.toDouble()) and 65535).toShort()
    }

    override fun byteValue(): Byte {
        return (floor(value.toDouble()) and 0xFF).toByte()
    }

    override fun doubleValue(): Double {
        return value.toDouble()
    }

    override fun floatValue(): Float {
        return this.value
    }

    override fun box(): Number {
        return this.value
    }


    override fun toString(): String {
        val stringTagVisitor = StringTagVisitor()
        stringTagVisitor.visitFloat(this)
        return stringTagVisitor.build()
    }

    companion object {
        val ZERO: FloatTag = FloatTag(0.0f)
        @JvmStatic
		fun valueOf(f: Float): FloatTag {
            return if (f == 0.0f) ZERO else FloatTag(f)
        }
    }
}
