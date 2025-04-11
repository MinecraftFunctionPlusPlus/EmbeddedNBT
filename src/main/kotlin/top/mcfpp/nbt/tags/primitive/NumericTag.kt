package top.mcfpp.nbt.tags.primitive

interface NumericTag<V> : PrimitiveTag<V> {
    fun byteValue(): Byte

    fun shortValue(): Short

    fun intValue(): Int

    fun longValue(): Long

    fun floatValue(): Float

    fun doubleValue(): Double

    fun box(): Number

    override fun asNumber(): Number {
        return this.box()
    }

    override fun asByte(): Byte {
        return this.byteValue()
    }

    override fun asShort(): Short {
        return this.shortValue()
    }

    override fun asInt(): Int {
        return this.intValue()
    }

    override fun asLong(): Long {
        return this.longValue()
    }

    override fun asFloat(): Float {
        return this.floatValue()
    }

    override fun asDouble(): Double {
        return this.doubleValue()
    }

    override fun asBoolean(): Boolean {
        return byteValue().toInt() != 0
    }

}

inline fun <reified N: Number> NumericTag<*>.asN(): N?{
    return when(N::class.javaPrimitiveType){
        Byte::class.javaPrimitiveType -> this.byteValue() as N
        Short::class.javaPrimitiveType -> this.shortValue() as N
        Int::class.javaPrimitiveType -> this.intValue() as N
        Long::class.javaPrimitiveType -> this.longValue() as N
        Float::class.javaPrimitiveType -> this.floatValue() as N
        Double::class.javaPrimitiveType -> this.doubleValue() as N
        else -> null
    }
}
