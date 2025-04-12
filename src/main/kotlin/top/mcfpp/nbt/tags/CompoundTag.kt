package top.mcfpp.nbt.tags

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
import java.util.function.BiConsumer

class CompoundTag(value: Map<String, Tag<*>> = HashMap()) : Tag<MutableMap<String, Tag<*>>>, Iterable<Map.Entry<String, Tag<*>>>{

    override val value: MutableMap<String, Tag<*>> = value.toMutableMap()

    constructor(vararg pairs: Pair<String, Any>) : this() {
        for (pair in pairs) {
            put(pair.first, pair.second)
        }
    }

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
        return when (e) {
            is Byte ->  value.put(string, valueOf(e))
            is Short ->  value.put(string, valueOf(e))
            is Int ->  value.put(string, valueOf(e))
            is Long ->  value.put(string, valueOf(e))
            is Float ->  value.put(string, valueOf(e))
            is Double ->  value.put(string, valueOf(e))
            is String ->  value.put(string, valueOf(e))
            is ByteArray ->  value.put(string, ByteArrayTag(e))
            is IntArray ->  value.put(string, IntArrayTag(e))
            is LongArray ->  value.put(string, LongArrayTag(e))
            is Boolean ->  value.put(string, valueOf(e))
            is Tag<*> ->  value.put(string, e)
            else -> throw IllegalStateException("Unexpected value: $e")
        }
    }

    operator fun set(string:String, e:Any) {
        put(string,e)
    }

    operator fun get(key: String): Tag<*>? {
        return value[key]
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

    override fun iterator(): Iterator<Map.Entry<String, Tag<*>>> {
        return value.iterator()
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

    fun containsKey(key: String): Boolean {
        return value.containsKey(key)
    }
}
