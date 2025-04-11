package top.mcfpp.nbt.tags.collection

import top.mcfpp.nbt.tags.CompoundTag
import top.mcfpp.nbt.tags.Tag
import top.mcfpp.nbt.tags.primitive.ByteTag
import top.mcfpp.nbt.tags.primitive.NumericTag
import top.mcfpp.nbt.tags.primitive.StringTag
import top.mcfpp.nbt.visitors.StringTagVisitor
import top.mcfpp.nbt.visitors.TagVisitor
import java.util.*
import java.util.Map
import java.util.stream.Stream
import kotlin.collections.ArrayList
import kotlin.collections.MutableList

@Suppress("unused")
class ListTag internal constructor(private val list: MutableList<Tag>) :  CollectionTag<Tag>,AbstractMutableList<Tag>(){
    constructor() : this(ArrayList<Tag>())

    override fun toString(): String {
        val stringTagVisitor = StringTagVisitor()
        stringTagVisitor.visitList(this)
        return stringTagVisitor.build()
    }

    override fun removeAt(index: Int): Tag {
        return list.removeAt(index)
    }


    fun <T:Any> get(i: Int, type: Class<T>): Optional<T> {
        val tag = list[i] ?: return Optional.empty()

        when {
            tag is CompoundTag && type == CompoundTag::class.java -> {
                return Optional.of<T>(type.cast(tag) as T)
            }
            tag is ListTag && type == ListTag::class.java -> {
                return Optional.of<T>(type.cast(tag) as T)
            }
            tag is ByteTag && type == Byte::class.javaPrimitiveType -> {
                return Optional.of<T>(type.cast(tag.byteValue()) as T)
            }
            tag is NumericTag && type == Short::class.javaPrimitiveType -> {
                return Optional.of<T>(type.cast(tag.shortValue()) as T)
            }
            tag is NumericTag && type == Int::class.javaPrimitiveType -> {
                return Optional.of<T>(type.cast(tag.intValue()) as T)
            }
            tag is IntArrayTag && type == IntArray::class.java -> {
                return Optional.of<T>(type.cast(tag.asIntArray) as T)
            }
            tag is LongArrayTag && type == LongArray::class.java -> {
                return Optional.of<T>(type.cast(tag.asLongArray) as T)
            }
            tag is NumericTag && type == Double::class.javaPrimitiveType -> {
                return Optional.of<T>(type.cast(tag.doubleValue()) as T)
            }
            tag is NumericTag && type == Float::class.javaPrimitiveType -> {
                return Optional.of<T>(type.cast(tag.floatValue()) as T)
            }
            tag is StringTag && type == String::class.java -> {
                return Optional.of<T>(type.cast(tag.asString()) as T)
            }
            else -> return Optional.empty()
        }
    }

    fun <T:Any> getOrDefault(i: Int, t: T, type: Class<T>): T {
        return this.get(i, type).orElse(t)
    }


    fun getCompoundOrEmpty(i: Int): CompoundTag {
        return this.getOrDefault(i, CompoundTag(), CompoundTag::class.java)
    }

    fun getListOrEmpty(i: Int): ListTag {
        return this.getOrDefault(i, ListTag(), ListTag::class.java)
    }



    override operator fun get(index: Int): Tag {
        return list[index]
    }

    override val size: Int
        get() = list.size

    override fun iterator(): MutableIterator<Tag> {
        return super<CollectionTag>.iterator()
    }



    override operator fun set(index: Int, tag: Tag): Tag {
        return list.set(index, tag)
    }

    override fun add(index: Int, tag: Tag) {
        list.add(index, tag)
    }

    override fun copy(): ListTag {
        val list: MutableList<Tag> = ArrayList(list.size)

        for (tag in this.list) {
            list.add(tag.copy())
        }

        return ListTag(list)
    }

    override fun asList(): Optional<ListTag> {
        return Optional.of(this)
    }

    override fun equals(`object`: Any?): Boolean {
        return this === `object` || `object` is ListTag && (this.list == `object`.list)
    }

    override fun hashCode(): Int {
        return list.hashCode()
    }

    override fun stream(): Stream<Tag> {
        return super<AbstractMutableList>.stream()
    }

    override fun accept(tagVisitor: TagVisitor) {
        tagVisitor.visitList(this)
    }

    override fun clear() {
        list.clear()
    }



    fun NeedWrap(): Boolean {
        if (list.size <= 1) return false
        val tag = list.first()
        return list.stream().anyMatch { t: Tag -> t.javaClass != tag.javaClass }
    }

    val wrappedList: ListTag
        get() {
            if (!NeedWrap()) return this
            val listTag = ListTag()
            for (tag in list) {
                listTag.add(CompoundTag(Map.of("", tag)))
            }
            return listTag
        }

    companion object {
        private const val WRAPPER_MARKER = ""
        @JvmStatic
		fun fromStream(stream: Stream<Tag>): ListTag {
            return ListTag(stream.toList())
        }
    }
}
