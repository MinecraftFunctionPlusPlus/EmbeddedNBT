package top.mcfpp.utils

import top.mcfpp.nbt.tags.CompoundTag
import top.mcfpp.nbt.tags.EndTag
import top.mcfpp.nbt.tags.Tag
import top.mcfpp.nbt.tags.collection.ByteArrayTag
import top.mcfpp.nbt.tags.collection.IntArrayTag
import top.mcfpp.nbt.tags.collection.ListTag
import top.mcfpp.nbt.tags.collection.LongArrayTag
import top.mcfpp.nbt.tags.primitive.ByteTag.Companion.valueOf
import top.mcfpp.nbt.tags.primitive.IntTag.Companion.valueOf
import top.mcfpp.nbt.tags.primitive.LongTag.Companion.valueOf
import top.mcfpp.nbt.tags.primitive.ShortTag.Companion.valueOf
import top.mcfpp.nbt.tags.primitive.StringTag
import top.mcfpp.nbt.tags.primitive.StringTag.Companion.valueOf
import java.nio.ByteBuffer
import java.util.stream.IntStream
import java.util.stream.LongStream
import java.util.stream.Stream

object TagUtils {

    @JvmStatic
    inline fun <reified T> `tag=`(value:T):Tag<*>{
        return when (value) {
            is String -> valueOf(value)
            is Short -> valueOf(value)
            is Int -> valueOf(value)
            is Long -> valueOf(value)
            is Byte -> valueOf(value)
            is Boolean -> valueOf(value)
            is Tag<*> -> value
            else -> EndTag.INSTANCE
        }
    }
    @JvmStatic
    fun `list=`(value:List<*>): ListTag {
        return ListTag.fromStream(value.map { `tag=`(it) }.stream())
    }

    @JvmStatic
    fun `list=`(vararg value:Any): ListTag {
        return ListTag.fromStream(value.map { `tag=`(it) }.stream())
    }

    @JvmStatic
    fun `list=`(block:(ListTag)->Unit): ListTag {
        return ListTag().apply(block)
    }


    @JvmStatic
    fun `array=`(value: IntArray):IntArrayTag{
        return IntArrayTag(value)
    }
    @JvmStatic
    fun `array=`(value: ByteArray):ByteArrayTag{
        return ByteArrayTag(value)
    }
    @JvmStatic
    fun `array=`(value:LongArray):LongArrayTag{
        return LongArrayTag(value)
    }
    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    inline fun <reified T> `array=`(vararg values:T):Tag<*>{
        when (T::class){
            Int::class -> return IntArrayTag((values as Array<Int>).toIntArray())
            Long::class -> return LongArrayTag((values as Array<Long>).toLongArray())
            Byte::class -> return ByteArrayTag((values as Array<Byte>).toByteArray())
        }
        return `list=`(values.toList())
    }

    @JvmStatic
    inline fun <reified T> CompoundTag.`term=`(pair: Pair<String,T>){
        this.put(pair.first, `tag=`(pair.second))
    }

    @JvmStatic
    fun `compound=`(block: CompoundTag.() -> Unit):CompoundTag{
        return CompoundTag().apply(block)
    }

    fun `compound=`(vararg values:Pair<String,*>):CompoundTag{
        val compoundTag = CompoundTag()
        for (pair in values){
            compoundTag.put(pair.first,`tag=`(pair.second))
        }
        return compoundTag
    }



    fun createMap():Tag<*> {
        return CompoundTag()
    }

    fun createMap(map: Map<Tag<*>, Tag<*>>): Tag<*> {
        return this.createMap(
            map.entries.stream().map { e ->
                Pair(
                    e.key,
                    e.value
                )
            }
        )
    }
    fun createMap(stream: Stream<Pair<Tag<*>, Tag<*>>>): Tag<*> {
        val compoundTag = CompoundTag()
        stream.forEach { pair ->
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

    fun createList():Tag<*>{
        return createList(Stream.of())
    }

    fun createList(stream: Stream<Tag<*>>): Tag<*> {
        return ListTag(stream.toList())
    }

    fun createByteList(byteBuffer: ByteBuffer): Tag<*> {
        val byteBuffer2 = byteBuffer.duplicate().clear()
        val bs = ByteArray(byteBuffer.capacity())
        byteBuffer2[0, bs, 0, bs.size]
        return ByteArrayTag(bs)
    }

    fun createIntList(intStream: IntStream): Tag<*> {
        return IntArrayTag(intStream.toArray())
    }

    fun createLongList(longStream: LongStream): Tag<*> {
        return LongArrayTag(longStream.toArray())
    }



}