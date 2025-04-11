package top.mcfpp.nbt.tags

import com.mojang.serialization.Codec
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.MapCodec
import top.mcfpp.nbt.NbtOps
import top.mcfpp.nbt.tags.collection.ByteArrayTag
import top.mcfpp.nbt.tags.collection.IntArrayTag
import top.mcfpp.nbt.tags.collection.ListTag
import top.mcfpp.nbt.tags.collection.LongArrayTag
import top.mcfpp.nbt.tags.primitive.ByteTag.Companion.valueOf
import top.mcfpp.nbt.tags.primitive.DoubleTag.Companion.valueOf
import top.mcfpp.nbt.tags.primitive.FloatTag.Companion.valueOf
import top.mcfpp.nbt.tags.primitive.IntTag.Companion.valueOf
import top.mcfpp.nbt.tags.primitive.LongTag.Companion.valueOf
import top.mcfpp.nbt.tags.primitive.ShortTag.Companion.valueOf
import top.mcfpp.nbt.tags.primitive.StringTag.Companion.valueOf
import top.mcfpp.nbt.visitors.StringTagVisitor
import top.mcfpp.nbt.visitors.TagVisitor
import java.util.*
import java.util.function.BiConsumer

class CompoundTag(override val value: MutableMap<String, Tag<*>> = HashMap()) : Tag<MutableMap<String, Tag<*>>> {
    fun keySet(): Set<String> {
        return value.keys
    }

    fun entrySet(): Set<Map.Entry<String, Tag<*>>> {
        return value.entries
    }

    fun values(): Collection<Tag<*>> {
        return value.values
    }

    fun forEach(biConsumer: BiConsumer<String, Tag<*>>?) {
        value.forEach(biConsumer!!)
    }

    fun size(): Int {
        return value.size
    }

    fun put(string: String, e: Any): Tag<*>? {
        when (e) {
            is Byte -> return value.put(string, valueOf(e))

            is Short -> return value.put(string, valueOf(e))

            is Int -> return value.put(string, valueOf(e))

            is Long -> return value.put(string, valueOf(e))

            is Float -> return value.put(string, valueOf(e))

            is Double -> return value.put(string, valueOf(e))

            is String -> return value.put(string, valueOf(e))

            is ByteArray -> return value.put(string, ByteArrayTag(e))

            is IntArray -> return value.put(string, IntArrayTag(e))

            is LongArray -> return value.put(string, LongArrayTag(e))

            is Boolean -> return value.put(string, valueOf(e))

            is Tag<*> -> return value.put(string, e)

            else -> throw IllegalStateException("Unexpected value: $e")
        }
    }

    operator fun set(string:String, e:Any) {
        put(string,e)
    }

    operator fun get(string: String): Tag<*>? {
        return value[string]
    }

    fun contains(string: String): Boolean {
        return value.containsKey(string)
    }


    inline fun <reified T : Any> getValue(key: String): T? = value[key]?.asT()

    inline fun <reified T:Any> getValueOrDefault(key: String, t: T): T {
        return this.getValue(key)?:t
    }

    fun getCompoundOrEmpty(key: String): CompoundTag {
        return this.getValueOrDefault(key, CompoundTag())
    }

    fun getListOrEmpty(key: String): ListTag {
        return this.getValueOrDefault(key, ListTag())
    }

    fun remove(string: String) {
        value.remove(string)
    }

    override fun toString(): String {
        val stringTagVisitor = StringTagVisitor()
        stringTagVisitor.visitCompound(this)
        return stringTagVisitor.build()
    }

    val isEmpty: Boolean
        get() = value.isEmpty()

    fun shallowCopy(): CompoundTag {
        return CompoundTag(HashMap(this.value))
    }

    override fun copy(): CompoundTag {
        val hashMap = HashMap<String, Tag<*>>()
        value.forEach { (string: String, tag: Tag<*>) -> hashMap[string] = tag.copy() }
        return CompoundTag(hashMap)
    }

    override fun asCompound(): CompoundTag {
        return this
    }

    override fun equals(other: Any?): Boolean {
        return this === other || other is CompoundTag && (this.value == other.value)
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }


    fun merge(compoundTag: CompoundTag): CompoundTag {
        for (string in compoundTag.value.keys) {
            val tag = compoundTag.value[string]
            val tagThis = value[string]
            if (tag is CompoundTag && tagThis is CompoundTag) {
                tagThis.merge(tag)
            } else {
                this.put(string, tag!!.copy())
            }
        }

        return this
    }

    override fun accept(tagVisitor: TagVisitor) {
        tagVisitor.visitCompound(this)
    }

    fun <T> store(string: String, codec: Codec<T>, `object`: T) {
        this.store(string, codec, NbtOps.INSTANCE, `object`)
    }

    fun <T> storeNullable(string: String, codec: Codec<T>, `object`: T?) {
        if (`object` != null) {
            this.store(string, codec, `object`)
        }
    }

    fun <T> store(string: String, codec: Codec<T>, dynamicOps: DynamicOps<Tag<*>>?, `object`: T) {
        this.put(string, codec.encodeStart(dynamicOps, `object`).getOrThrow())
    }

    fun <T> storeNullable(string: String, codec: Codec<T>, dynamicOps: DynamicOps<Tag<*>>?, `object`: T?) {
        if (`object` != null) {
            this.store(string, codec, dynamicOps, `object`)
        }
    }

    fun <T> store(mapCodec: MapCodec<T>, `object`: T) {
        this.store(mapCodec, NbtOps.INSTANCE, `object`)
    }

    fun <T> store(mapCodec: MapCodec<T>, dynamicOps: DynamicOps<Tag<*>>?, `object`: T) {
        this.merge(mapCodec.encoder().encodeStart(dynamicOps, `object`).getOrThrow() as CompoundTag)
    }

    fun <T:Any> read(string: String, codec: Codec<T>): Optional<T> {
        return this.read(string, codec, NbtOps.INSTANCE)
    }

    fun <T:Any> read(string: String, codec: Codec<T>, dynamicOps: DynamicOps<Tag<*>>?): Optional<T> {
        val tag = this[string]
        return if (tag == null
        ) Optional.empty()
        else codec.parse(dynamicOps, tag).resultOrPartial { string2: String -> }
    }

    fun <T:Any> read(mapCodec: MapCodec<T>): Optional<T> {
        return this.read<T>(mapCodec, NbtOps.INSTANCE)
    }

    fun <T:Any> read(mapCodec: MapCodec<T>, dynamicOps: DynamicOps<Tag<*>>): Optional<T> {
        return mapCodec.decode(dynamicOps, dynamicOps.getMap(this).getOrThrow())
            .resultOrPartial { string: String -> }
    }
}
