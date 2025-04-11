package top.mcfpp.nbt.tags

import top.mcfpp.nbt.tags.collection.ByteArrayTag
import top.mcfpp.nbt.tags.collection.IntArrayTag
import top.mcfpp.nbt.tags.collection.ListTag
import top.mcfpp.nbt.tags.collection.LongArrayTag
import top.mcfpp.nbt.tags.primitive.NumericTag
import top.mcfpp.nbt.tags.primitive.StringTag
import top.mcfpp.nbt.tags.primitive.asN
import top.mcfpp.nbt.visitors.TagVisitor

interface Tag<V> {

    val value: V

    override fun toString(): String

    fun copy(): Tag<V>

    fun accept(tagVisitor: TagVisitor)

    fun asString(): String? {
        return null
    }

    fun asNumber(): Number? {
        return null
    }

    fun asByte(): Byte? {
        return asNumber()?.toByte()
    }

    fun asShort(): Short? {
        return asNumber()?.toShort()
    }

    fun asInt(): Int? {
        return asNumber()?.toInt()
    }

    fun asLong(): Long? {
        return asNumber()?.toLong()
    }

    fun asFloat(): Float? {
        return asNumber()?.toFloat()
    }

    fun asDouble(): Double? {
        return asNumber()?.toDouble()
    }

    fun asBoolean(): Boolean {
        return asByte()?.let { it.toInt() != 0 }?:false
    }

    fun asByteArray(): ByteArray? {
        return null
    }

    fun asIntArray(): IntArray? {
        return null
    }

    fun asLongArray(): LongArray? {
        return null
    }

    fun asCompound(): CompoundTag? {
        return null
    }

    fun asList(): ListTag? {
        return null
    }
}

inline fun <reified T: Any> Tag<*>.asT(): T?{
    return when{
        Tag::class.java.isAssignableFrom(T::class.java) -> this as T
        this is NumericTag -> this.asN()
        this is ByteArrayTag && T::class.java == ByteArray::class.java -> this.value as T
        this is IntArrayTag && T::class.java == IntArray::class.java -> this.value as T
        this is LongArrayTag && T::class.java == LongArray::class.java -> this.value as T
        this is StringTag && T::class.java == String::class.java -> this.asString() as T
        value == null -> null
        T::class.java.isAssignableFrom(this.value!!::class.java) -> this.value as T
        else -> null
    }
}
