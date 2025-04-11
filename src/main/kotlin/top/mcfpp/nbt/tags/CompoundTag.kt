package top.mcfpp.nbt.tags

import com.mojang.serialization.Codec
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.MapCodec
import top.mcfpp.nbt.NbtOps
import top.mcfpp.nbt.tags.collection.ByteArrayTag
import top.mcfpp.nbt.tags.collection.IntArrayTag
import top.mcfpp.nbt.tags.collection.ListTag
import top.mcfpp.nbt.tags.collection.LongArrayTag
import top.mcfpp.nbt.tags.primitive.ByteTag
import top.mcfpp.nbt.tags.primitive.ByteTag.Companion.valueOf
import top.mcfpp.nbt.tags.primitive.DoubleTag.Companion.valueOf
import top.mcfpp.nbt.tags.primitive.FloatTag.Companion.valueOf
import top.mcfpp.nbt.tags.primitive.IntTag.Companion.valueOf
import top.mcfpp.nbt.tags.primitive.LongTag.Companion.valueOf
import top.mcfpp.nbt.tags.primitive.NumericTag
import top.mcfpp.nbt.tags.primitive.ShortTag.Companion.valueOf
import top.mcfpp.nbt.tags.primitive.StringTag
import top.mcfpp.nbt.tags.primitive.StringTag.Companion.valueOf
import top.mcfpp.nbt.visitors.StringTagVisitor
import top.mcfpp.nbt.visitors.TagVisitor
import java.util.*
import java.util.function.BiConsumer

class CompoundTag @JvmOverloads constructor(private val tags: MutableMap<String, Tag> = HashMap()) : Tag {
    fun keySet(): Set<String> {
        return tags.keys
    }

    fun entrySet(): Set<Map.Entry<String, Tag>> {
        return tags.entries
    }

    fun values(): Collection<Tag> {
        return tags.values
    }

    fun forEach(biConsumer: BiConsumer<String, Tag>?) {
        tags.forEach(biConsumer!!)
    }

    fun size(): Int {
        return tags.size
    }

    fun put(string: String, e: Any): Tag? {
        when (e) {
            is Byte -> return tags.put(string, ByteTag.valueOf(e))

            is Short -> return tags.put(string, valueOf(e))

            is Int -> return tags.put(string, valueOf(e))

            is Long -> return tags.put(string, valueOf(e))

            is Float -> return tags.put(string, valueOf(e))

            is Double -> return tags.put(string, valueOf(e))

            is String -> return tags.put(string, valueOf(e))

            is ByteArray -> return tags.put(string, ByteArrayTag(e))

            is IntArray -> return tags.put(string, IntArrayTag(e))

            is LongArray -> return tags.put(string, LongArrayTag(e))

            is Boolean -> return tags.put(string, valueOf(e))

            is Tag -> return tags.put(string, e)

            else -> throw IllegalStateException("Unexpected value: $e")
        }
    }

    operator fun set(string:String, e:Any): Unit{
        put(string,e)
    }

    operator fun get(string: String): Tag? {
        return tags[string]
    }

    fun contains(string: String): Boolean {
        return tags.containsKey(string)
    }


    fun <T:Any> get(key: String, type: Class<T>): Optional<T> {
        val tag = tags[key] ?: return Optional.empty()

        when {
            tag is CompoundTag && type == CompoundTag::class.java -> {
                return Optional.of<T>(type.cast(tag) as T)
            }
            tag is ListTag && type == ListTag::class.java -> {
                return Optional.of<T>(type.cast(tag)  as T)
            }
            tag is ByteTag && type == Byte::class.javaPrimitiveType -> {
                return Optional.of<T>(type.cast(tag.byteValue())  as T)
            }
            tag is NumericTag && type == Short::class.javaPrimitiveType -> {
                return Optional.of<T>(type.cast(tag.shortValue())  as T)
            }
            tag is NumericTag && type == Int::class.javaPrimitiveType -> {
                return Optional.of<T>(type.cast(tag.intValue())  as T)
            }
            tag is IntArrayTag && type == IntArray::class.java -> {
                return Optional.of<T>(type.cast(tag.asIntArray)  as T)
            }
            tag is LongArrayTag && type == LongArray::class.java -> {
                return Optional.of<T>(type.cast(tag.asLongArray)  as T)
            }
            tag is NumericTag && type == Double::class.javaPrimitiveType -> {
                return Optional.of<T>(type.cast(tag.doubleValue())  as T)
            }
            tag is NumericTag && type == Float::class.javaPrimitiveType -> {
                return Optional.of<T>(type.cast(tag.floatValue())  as T)
            }
            tag is StringTag && type == String::class.java -> {
                return Optional.of<T>(type.cast(tag.asString())  as T)
            }
            else -> return Optional.empty()
        }
    }

    fun <T:Any> getOrDefault(key: String, t: T, type: Class<T>): T {
        return this.get(key, type).orElse(t)
    }

    fun getOptional(string: String): Optional<Tag> {
        return Optional.ofNullable(tags[string])
    }

    fun getCompoundOrEmpty(key: String): CompoundTag {
        return this.getOrDefault(key, CompoundTag(), CompoundTag::class.java)
    }

    fun getListOrEmpty(key: String): ListTag {
        return this.getOrDefault(key, ListTag(), ListTag::class.java)
    }

    fun remove(string: String) {
        tags.remove(string)
    }

    override fun toString(): String {
        val stringTagVisitor = StringTagVisitor()
        stringTagVisitor.visitCompound(this)
        return stringTagVisitor.build()
    }

    val isEmpty: Boolean
        get() = tags.isEmpty()

    fun shallowCopy(): CompoundTag {
        return CompoundTag(HashMap(this.tags))
    }

    override fun copy(): CompoundTag {
        val hashMap = HashMap<String, Tag>()
        tags.forEach { (string: String, tag: Tag) -> hashMap[string] = tag.copy() }
        return CompoundTag(hashMap)
    }

    override fun asCompound(): Optional<CompoundTag> {
        return Optional.of(this)
    }

    override fun equals(`object`: Any?): Boolean {
        return this === `object` || `object` is CompoundTag && (this.tags == `object`.tags)
    }

    override fun hashCode(): Int {
        return tags.hashCode()
    }


    fun merge(compoundTag: CompoundTag): CompoundTag {
        for (string in compoundTag.tags.keys) {
            val tag = compoundTag.tags[string]
            val tagThis = tags[string]
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

    fun <T> store(string: String, codec: Codec<T>, dynamicOps: DynamicOps<Tag>?, `object`: T) {
        this.put(string, codec.encodeStart(dynamicOps, `object`).getOrThrow())
    }

    fun <T> storeNullable(string: String, codec: Codec<T>, dynamicOps: DynamicOps<Tag>?, `object`: T?) {
        if (`object` != null) {
            this.store(string, codec, dynamicOps, `object`)
        }
    }

    fun <T> store(mapCodec: MapCodec<T>, `object`: T) {
        this.store(mapCodec, NbtOps.INSTANCE, `object`)
    }

    fun <T> store(mapCodec: MapCodec<T>, dynamicOps: DynamicOps<Tag>?, `object`: T) {
        this.merge(mapCodec.encoder().encodeStart(dynamicOps, `object`).getOrThrow() as CompoundTag)
    }

    fun <T:Any> read(string: String, codec: Codec<T>): Optional<T> {
        return this.read(string, codec, NbtOps.INSTANCE)
    }

    fun <T:Any> read(string: String, codec: Codec<T>, dynamicOps: DynamicOps<Tag>?): Optional<T> {
        val tag = this.get(string)
        return if (tag == null
        ) Optional.empty()
        else codec.parse(dynamicOps, tag).resultOrPartial { string2: String -> }
    }

    fun <T:Any> read(mapCodec: MapCodec<T>): Optional<T> {
        return this.read<T>(mapCodec, NbtOps.INSTANCE)
    }

    fun <T:Any> read(mapCodec: MapCodec<T>, dynamicOps: DynamicOps<Tag>): Optional<T> {
        return mapCodec.decode(dynamicOps, dynamicOps.getMap(this).getOrThrow())
            .resultOrPartial { string: String -> }
    }

    companion object {
        private const val WRAPPER_MARKER = ""
    }
}
