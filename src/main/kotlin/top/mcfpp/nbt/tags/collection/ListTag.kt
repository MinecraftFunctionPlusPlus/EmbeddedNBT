package top.mcfpp.nbt.tags.collection

import top.mcfpp.nbt.tags.CompoundTag
import top.mcfpp.nbt.tags.Tag
import top.mcfpp.nbt.tags.asT
import top.mcfpp.nbt.visitors.StringTagVisitor
import top.mcfpp.nbt.visitors.TagVisitor
import java.util.stream.Stream

@Suppress("unused")
class ListTag(list: List<Tag<*>>) : CollectionTag<Tag<*>, MutableList<Tag<*>>>,AbstractMutableList<Tag<*>>(){

    constructor(list: Stream<Tag<*>>) : this(list.toList())


    override val value: MutableList<Tag<*>> = ArrayList(list)

    constructor() : this(ArrayList<Tag<*>>())

    override fun toString(): String {
        val stringTagVisitor = StringTagVisitor()
        stringTagVisitor.visitList(this)
        return stringTagVisitor.build()
    }

    override fun removeAt(index: Int): Tag<*> {
        return value.removeAt(index)
    }

    inline fun <reified T : Any> getValue(i: Int): T? = value[i].asT()

    inline fun <reified T:Any> getValueOrDefault(i: Int, t: T): T {
        return this.getValue(i)?:t
    }

    fun getCompoundOrEmpty(i: Int): CompoundTag {
        return this.getValueOrDefault(i, CompoundTag())
    }

    fun getListOrEmpty(i: Int): ListTag {
        return this.getValueOrDefault(i, ListTag())
    }

    override operator fun get(index: Int): Tag<*> {
        return value[index]
    }

    override val size: Int
        get() = value.size

    override fun iterator(): MutableIterator<Tag<*>> {
        return super<CollectionTag>.iterator()
    }



    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override operator fun set(index: Int, tag: Tag<*>): Tag<*> {
        return value.set(index, tag)
    }

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun add(index: Int, tag: Tag<*>) {
        value.add(index, tag)
    }

    override fun copy(): ListTag {
        val list: MutableList<Tag<*>> = ArrayList(value.size)

        for (tag in this.value) {
            list.add(tag.copy())
        }

        return ListTag(list)
    }

    override fun asList(): ListTag {
        return this
    }

    override fun equals(other: Any?): Boolean {
        return this === other || other is ListTag && (this.value == other.value)
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun stream(): Stream<Tag<*>> {
        return super<AbstractMutableList>.stream()
    }

    override fun accept(tagVisitor: TagVisitor) {
        tagVisitor.visitList(this)
    }

    override fun clear() {
        value.clear()
    }

    fun needWrap(): Boolean {
        if (value.size <= 1) return false
        val tag = value.first()
        return value.stream().anyMatch { t: Tag<*> -> t.javaClass != tag.javaClass }
    }

    val wrappedList: ListTag
        get() {
            if (!needWrap()) return this
            val listTag = ListTag()
            for (tag in value) {
                listTag.add(CompoundTag(mutableMapOf("" to tag)))
            }
            return listTag
        }

    companion object {
        private const val WRAPPER_MARKER = ""
        @JvmStatic
		fun fromStream(stream: Stream<Tag<*>>): ListTag {
            return ListTag(stream.toList())
        }
    }
}
