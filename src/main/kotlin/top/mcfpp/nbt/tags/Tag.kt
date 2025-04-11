package top.mcfpp.nbt.tags

import top.mcfpp.nbt.tags.collection.ListTag
import top.mcfpp.nbt.visitors.TagVisitor
import java.util.*

interface Tag {
    override fun toString(): String

    fun copy(): Tag

    fun accept(tagVisitor: TagVisitor)

    fun asString(): Optional<String> {
        return Optional.empty()
    }

    fun asNumber(): Optional<Number> {
        return Optional.empty()
    }

    fun asByte(): Optional<Byte> {
        return asNumber().map { obj: Number -> obj.toByte() }
    }

    fun asShort(): Optional<Short> {
        return asNumber().map { obj: Number -> obj.toShort() }
    }

    fun asInt(): Optional<Int> {
        return asNumber().map { obj: Number -> obj.toInt() }
    }

    fun asLong(): Optional<Long> {
        return asNumber().map { obj: Number -> obj.toLong() }
    }

    fun asFloat(): Optional<Float> {
        return asNumber().map { obj: Number -> obj.toFloat() }
    }

    fun asDouble(): Optional<Double> {
        return asNumber().map { obj: Number -> obj.toDouble() }
    }

    fun asBoolean(): Optional<Boolean> {
        return asByte().map { byte_: Byte -> byte_.toInt() != 0 }
    }

    fun asByteArray(): Optional<ByteArray> {
        return Optional.empty()
    }

    fun asIntArray(): Optional<IntArray> {
        return Optional.empty()
    }

    fun asLongArray(): Optional<LongArray> {
        return Optional.empty()
    }

    fun asCompound(): Optional<CompoundTag> {
        return Optional.empty()
    }

    fun asList(): Optional<ListTag> {
        return Optional.empty()
    }
}
