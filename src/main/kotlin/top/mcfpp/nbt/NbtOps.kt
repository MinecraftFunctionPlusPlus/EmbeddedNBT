package top.mcfpp.nbt

import com.mojang.datafixers.util.Pair
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.MapLike
import com.mojang.serialization.RecordBuilder
import it.unimi.dsi.fastutil.bytes.ByteArrayList
import it.unimi.dsi.fastutil.bytes.ByteConsumer
import it.unimi.dsi.fastutil.ints.IntArrayList
import it.unimi.dsi.fastutil.ints.IntConsumer
import it.unimi.dsi.fastutil.longs.LongArrayList
import it.unimi.dsi.fastutil.longs.LongConsumer
import top.mcfpp.nbt.tags.CompoundTag
import top.mcfpp.nbt.tags.EndTag
import top.mcfpp.nbt.tags.Tag
import top.mcfpp.nbt.tags.collection.*
import top.mcfpp.nbt.tags.collection.ListTag.Companion.fromStream
import top.mcfpp.nbt.tags.primitive.*
import top.mcfpp.nbt.tags.primitive.ByteTag.Companion.valueOf
import top.mcfpp.nbt.tags.primitive.DoubleTag.Companion.valueOf
import top.mcfpp.nbt.tags.primitive.FloatTag.Companion.valueOf
import top.mcfpp.nbt.tags.primitive.IntTag.Companion.valueOf
import top.mcfpp.nbt.tags.primitive.LongTag.Companion.valueOf
import top.mcfpp.nbt.tags.primitive.ShortTag.Companion.valueOf
import top.mcfpp.nbt.tags.primitive.StringTag.Companion.valueOf
import java.nio.ByteBuffer
import java.util.*
import java.util.function.BiConsumer
import java.util.function.Consumer
import java.util.function.Function
import java.util.stream.IntStream
import java.util.stream.LongStream
import java.util.stream.Stream

class NbtOps private constructor() : DynamicOps<Tag> {
    override fun empty(): Tag {
        return EndTag.INSTANCE
    }

    override fun <U:Any> convertTo(dynamicOps: DynamicOps<U>, tag: Tag): U {
        val u: U = when (tag) {
            is EndTag -> dynamicOps.empty()
            is ByteTag -> dynamicOps.createByte(tag.value)
            is ShortTag -> dynamicOps.createShort(tag.value)
            is IntTag -> dynamicOps.createInt(tag.value)
            is LongTag -> dynamicOps.createLong(tag.value)
            is FloatTag -> dynamicOps.createFloat(tag.value)
            is DoubleTag -> dynamicOps.createDouble(tag.value)
            is ByteArrayTag -> dynamicOps.createByteList(ByteBuffer.wrap(tag.asByteArray))
            is StringTag -> dynamicOps.createString(tag.value)
            is ListTag -> this.convertList(dynamicOps, tag)
            is CompoundTag -> this.convertMap(dynamicOps, tag)
            is IntArrayTag -> dynamicOps.createIntList(Arrays.stream(tag.asIntArray))
            is LongArrayTag -> dynamicOps.createLongList(Arrays.stream(tag.asLongArray))
            else -> throw MatchException(null, null)
        }
        return u
    }

    override fun getNumberValue(tag: Tag): DataResult<Number> {
        return tag.asNumber().map<DataResult<Number>> { result: Number? -> DataResult.success(result) }
            .orElseGet { DataResult.error { "Not a number" } }
    }

    override fun createNumeric(number: Number): Tag {
        return valueOf(number.toDouble())
    }

    override fun createByte(b: Byte): Tag {
        return valueOf(b)
    }

    override fun createShort(s: Short): Tag {
        return valueOf(s)
    }

    override fun createInt(i: Int): Tag {
        return valueOf(i)
    }

    override fun createLong(l: Long): Tag {
        return valueOf(l)
    }

    override fun createFloat(f: Float): Tag {
        return valueOf(f)
    }

    override fun createDouble(d: Double): Tag {
        return valueOf(d)
    }

    override fun createBoolean(bl: Boolean): Tag {
        return valueOf(bl)
    }

    override fun getStringValue(tag: Tag): DataResult<String> {
        return if (tag is StringTag) DataResult.success(tag.value) else DataResult.error { "Not a string" }
    }

    override fun createString(string: String): Tag {
        return valueOf(string)
    }

    override fun mergeToList(tag: Tag, tag2: Tag): DataResult<Tag> {
        return createCollector(tag)
            .map { listCollector: ListCollector -> DataResult.success(listCollector.accept(tag2).result()) }
            .orElseGet {
                DataResult.error(
                    { "mergeToList called with not a list: $tag" }, tag
                )
            }
    }

    override fun mergeToList(tag: Tag, list: List<Tag>): DataResult<Tag> {
        return createCollector(tag)
            .map { listCollector: ListCollector -> DataResult.success(listCollector.acceptAll(list).result()) }
            .orElseGet {
                DataResult.error(
                    { "mergeToList called with not a list: $tag" }, tag
                )
            }
    }

    override fun mergeToMap(tag: Tag, tag2: Tag, tag3: Tag): DataResult<Tag> {
        if (tag !is CompoundTag && tag !is EndTag) {
            return DataResult.error({ "mergeToMap called with not a map: $tag" }, tag)
        } else if (tag2 is StringTag) {
            val compoundTag = tag2.value
            val compoundTag2 = if (tag is CompoundTag) tag.shallowCopy() else CompoundTag()
            compoundTag2.put(compoundTag, tag3)
            return DataResult.success(compoundTag2)
        } else {
            return DataResult.error({ "key is not a string: $tag2" }, tag)
        }
    }

    override fun mergeToMap(tag: Tag, mapLike: MapLike<Tag>): DataResult<Tag> {
        if (tag !is CompoundTag && tag !is EndTag) {
            return DataResult.error({ "mergeToMap called with not a map: $tag" }, tag)
        } else {
            val compoundTag2 = if (tag is CompoundTag) tag.shallowCopy() else CompoundTag()
            val list: MutableList<Tag> = ArrayList()
            mapLike.entries().forEach { pair: Pair<Tag, Tag> ->
                val tagx = pair.first
                if (tagx is StringTag) {
                    compoundTag2.put(tagx.value, pair.second)
                } else {
                    list.add(tagx)
                }
            }
            return if (!list.isEmpty()) DataResult.error(
                { "some keys are not strings: $list" },
                compoundTag2
            ) else DataResult.success(compoundTag2)
        }
    }

    override fun mergeToMap(tag: Tag, map: Map<Tag, Tag>): DataResult<Tag> {
        if (tag !is CompoundTag && tag !is EndTag) {
            return DataResult.error({ "mergeToMap called with not a map: $tag" }, tag)
        } else {
            val compoundTag2 = if (tag is CompoundTag) tag.shallowCopy() else CompoundTag()
            val list: MutableList<Tag> = ArrayList()

            for ((tag2, value) in map) {
                if (tag2 is StringTag) {
                    compoundTag2.put(tag2.value, value)
                } else {
                    list.add(tag2)
                }
            }

            return if (!list.isEmpty()) DataResult.error(
                { "some keys are not strings: $list" },
                compoundTag2
            ) else DataResult.success(compoundTag2)
        }
    }

    override fun getMapValues(tag: Tag): DataResult<Stream<Pair<Tag, Tag>>> {
        return if (tag is CompoundTag
        ) DataResult.success(
            tag.entrySet().stream().map { entry: Map.Entry<String, Tag> ->
                Pair.of(
                    this.createString(entry.key), entry.value
                )
            }
        )
        else DataResult.error { "Not a map: $tag" }
    }

    override fun getMapEntries(tag: Tag): DataResult<Consumer<BiConsumer<Tag, Tag>>> {
        return if (tag is CompoundTag) DataResult.success(
            Consumer { biConsumer: BiConsumer<Tag, Tag> ->
                for ((key, value) in tag.entrySet()) {
                    biConsumer.accept(this.createString(key!!), value)
                }
            }) else DataResult.error { "Not a map: $tag" }
    }

    override fun getMap(tag: Tag): DataResult<MapLike<Tag>> {
        return if (tag is CompoundTag) DataResult.success(object : MapLike<Tag> {
            override fun get(tagx: Tag?): Tag? {
                if (tagx is StringTag) {
                    return tag.get(tagx.value)
                } else {
                    throw UnsupportedOperationException("Cannot get map entry with non-string key: $tag")
                }
            }

            override fun get(string: String): Tag? {
                return tag.get(string)
            }

            override fun entries(): Stream<Pair<Tag?, Tag?>> {
                return tag.entrySet().stream().map(
                    Function<Map.Entry<String, Tag?>, Pair<Tag?, Tag?>> { entry: Map.Entry<String, Tag?> ->
                        Pair.of(
                            this@NbtOps.createString(entry.key), entry.value
                        )
                    })
            }

            override fun toString(): String {
                return "MapLike[$tag]"
            }
        }) else DataResult.error { "Not a map: $tag" }
    }

    override fun createMap(stream: Stream<Pair<Tag, Tag>>): Tag {
        val compoundTag = CompoundTag()
        stream.forEach { pair: Pair<Tag, Tag> ->
            val tag = pair.first
            val tag2 = pair.second
            if (tag is StringTag) {
                compoundTag.put(tag.value, tag2)
            } else {
                throw UnsupportedOperationException("Cannot create map with non-string key: $tag")
            }
        }
        return compoundTag
    }

    override fun getStream(tag: Tag): DataResult<Stream<Tag>> {
        return if (tag is CollectionTag<*>) DataResult.success(tag.stream() as Stream<Tag>) else DataResult.error { "Not a list" }
    }

    override fun getList(tag: Tag): DataResult<Consumer<Consumer<Tag>>> {
        return if (tag is CollectionTag<*>) DataResult.success(
            Consumer { action: Consumer<Tag> -> tag.forEach(action) }) else DataResult.error { "Not a list: $tag" }
    }

    override fun getByteBuffer(tag: Tag): DataResult<ByteBuffer> {
        return if (tag is ByteArrayTag) DataResult.success(ByteBuffer.wrap(tag.asByteArray)) else super.getByteBuffer(
            tag
        )
    }

    override fun createByteList(byteBuffer: ByteBuffer): Tag {
        val byteBuffer2 = byteBuffer.duplicate().clear()
        val bs = ByteArray(byteBuffer.capacity())
        byteBuffer2[0, bs, 0, bs.size]
        return ByteArrayTag(bs)
    }

    override fun getIntStream(tag: Tag): DataResult<IntStream> {
        return if (tag is IntArrayTag) DataResult.success(Arrays.stream(tag.asIntArray)) else super.getIntStream(tag)
    }

    override fun createIntList(intStream: IntStream): Tag {
        return IntArrayTag(intStream.toArray())
    }

    override fun getLongStream(tag: Tag): DataResult<LongStream> {
        return if (tag is LongArrayTag) DataResult.success(Arrays.stream(tag.asLongArray)) else super.getLongStream(tag)
    }

    override fun createLongList(longStream: LongStream): Tag {
        return LongArrayTag(longStream.toArray())
    }

    override fun createList(stream: Stream<Tag>): Tag {
        return fromStream(stream)
    }

    override fun remove(tag: Tag, string: String): Tag {
        if (tag is CompoundTag) {
            val compoundTag2: CompoundTag = tag.shallowCopy()
            compoundTag2.remove(string)
            return compoundTag2
        } else {
            return tag
        }
    }

    override fun toString(): String {
        return "NBT"
    }

    override fun mapBuilder(): RecordBuilder<Tag> {
        return NbtRecordBuilder()
    }

    internal class ByteListCollector(bs: ByteArray?) : ListCollector {
        private val values = ByteArrayList()

        init {
            values.addElements(0, bs)
        }

        override fun accept(tag: Tag): ListCollector {
            if (tag is ByteTag) {
                values.add(tag.byteValue())
                return this
            } else {
                return GenericListCollector(this.values).accept(tag)
            }
        }

        override fun result(): Tag {
            return ByteArrayTag(values.toByteArray())
        }
    }

    internal class GenericListCollector : ListCollector {
        private val result = ListTag()

        constructor()

        constructor(listTag: ListTag) {
            result.addAll(listTag)
        }

        constructor(intArrayList: IntArrayList) {
            intArrayList.forEach(IntConsumer { i: Int -> result.add(valueOf(i)) })
        }

        constructor(byteArrayList: ByteArrayList) {
            byteArrayList.forEach(ByteConsumer { b: Byte -> result.add(valueOf(b)) })
        }

        constructor(longArrayList: LongArrayList) {
            longArrayList.forEach(LongConsumer { l: Long -> result.add(valueOf(l)) })
        }

        override fun accept(tag: Tag): ListCollector {
            result.add(tag)
            return this
        }

        override fun result(): Tag {
            return this.result
        }
    }

    internal class IntListCollector(`is`: IntArray?) : ListCollector {
        private val values = IntArrayList()

        init {
            values.addElements(0, `is`)
        }

        override fun accept(tag: Tag): ListCollector {
            if (tag is IntTag) {
                values.add(tag.intValue())
                return this
            } else {
                return GenericListCollector(this.values).accept(tag)
            }
        }

        override fun result(): Tag {
            return IntArrayTag(values.toIntArray())
        }
    }

    internal interface ListCollector {
        fun accept(tag: Tag): ListCollector

        fun acceptAll(iterable: Iterable<Tag>): ListCollector {
            var listCollector = this

            for (tag in iterable) {
                listCollector = listCollector.accept(tag)
            }

            return listCollector
        }

        fun acceptAll(stream: Stream<Tag>): ListCollector {
            return this.acceptAll(Iterable { stream.iterator() })
        }

        fun result(): Tag
    }

    internal class LongListCollector(ls: LongArray?) : ListCollector {
        private val values = LongArrayList()

        init {
            values.addElements(0, ls)
        }

        override fun accept(tag: Tag): ListCollector {
            if (tag is LongTag) {
                values.add(tag.longValue())
                return this
            } else {
                return GenericListCollector(this.values).accept(tag)
            }
        }

        override fun result(): Tag {
            return LongArrayTag(values.toLongArray())
        }
    }

    internal inner class NbtRecordBuilder : RecordBuilder.AbstractStringBuilder<Tag, CompoundTag>(
        this@NbtOps
    ) {
        override fun initBuilder(): CompoundTag {
            return CompoundTag()
        }

        override fun append(string: String, tag: Tag?, compoundTag: CompoundTag): CompoundTag {
            compoundTag.put(string, tag!!)
            return compoundTag
        }

        override fun build(compoundTag: CompoundTag, tag: Tag?): DataResult<Tag?> {
            if (tag == null || tag === EndTag.INSTANCE) {
                return DataResult.success(compoundTag)
            } else if (tag !is CompoundTag) {
                return DataResult.error({ "mergeToMap called with not a map: $tag" }, tag)
            } else {
                val compoundTag3: CompoundTag = tag.shallowCopy()

                for ((key, value) in compoundTag.entrySet()) {
                    compoundTag3.put(key, value)
                }

                return DataResult.success(compoundTag3)
            }
        }
    }

    companion object {
        val INSTANCE: NbtOps = NbtOps()

        private fun createCollector(tag: Tag): Optional<ListCollector> {
            when (tag) {
                is EndTag -> return Optional.of(GenericListCollector())

                is CollectionTag<*> -> return if (tag.isEmpty) {
                    Optional.of(GenericListCollector())
                } else {
                    when (tag) {
                        is ListTag -> Optional.of(GenericListCollector(tag))

                        is ByteArrayTag -> Optional.of(ByteListCollector(tag.asByteArray))

                        is IntArrayTag -> Optional.of(IntListCollector(tag.asIntArray))

                        is LongArrayTag -> Optional.of(LongListCollector(tag.asLongArray))

                        else -> throw MatchException(null, null)
                    }
                }

                else -> return Optional.empty()
            }
        }
    }
}
