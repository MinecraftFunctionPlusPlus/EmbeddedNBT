package top.mcfpp.nbt.tags.collection

import top.mcfpp.nbt.tags.Tag
import java.util.stream.Stream
import java.util.stream.StreamSupport

interface CollectionTag<T : Tag> : Iterable<T>, Tag {
    fun clear()

    operator fun set(index: Int, tag: T): T

    fun add(index: Int, tag: T)

    fun removeAt(index: Int): T

    operator fun get(index: Int): T

    val size: Int

    val isEmpty: Boolean
        get() = this.size == 0

    override fun iterator(): MutableIterator<T> {
        return object : MutableIterator<T> {
            private var index = 0
            private var lastReturnedIndex = -1 // 记录最近返回的元素索引，用于 remove()

            override fun hasNext(): Boolean {
                return this.index < this@CollectionTag.size
            }

            override fun next(): T {
                if (!this.hasNext()) {
                    throw NoSuchElementException()
                } else {
                    lastReturnedIndex = index
                    return this@CollectionTag.get(index++)
                }
            }

            override fun remove() {
                if (lastReturnedIndex == -1) {
                    throw IllegalStateException("next() must be called before remove()")
                }
                this@CollectionTag.removeAt(lastReturnedIndex)
                index-- // 因为删除了一个元素，索引回退
                lastReturnedIndex = -1 // 防止重复 remove()
            }
        }
    }

    fun stream(): Stream<T> {
        return StreamSupport.stream(this.spliterator(), false)
    }
}
