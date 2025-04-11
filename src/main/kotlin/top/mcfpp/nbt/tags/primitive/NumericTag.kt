package top.mcfpp.nbt.tags.primitive

import java.util.*

interface NumericTag : PrimitiveTag {
    fun byteValue(): Byte

    fun shortValue(): Short

    fun intValue(): Int

    fun longValue(): Long

    fun floatValue(): Float

    fun doubleValue(): Double

    fun box(): Number

    override fun asNumber(): Optional<Number> {
        return Optional.of(this.box())
    }

    override fun asByte(): Optional<Byte> {
        return Optional.of(this.byteValue())
    }

    override fun asShort(): Optional<Short> {
        return Optional.of(this.shortValue())
    }

    override fun asInt(): Optional<Int> {
        return Optional.of(this.intValue())
    }

    override fun asLong(): Optional<Long> {
        return Optional.of(this.longValue())
    }

    override fun asFloat(): Optional<Float> {
        return Optional.of(this.floatValue())
    }

    override fun asDouble(): Optional<Double> {
        return Optional.of(this.doubleValue())
    }

    override fun asBoolean(): Optional<Boolean> {
        return Optional.of(byteValue().toInt() != 0)
    }
}
