package top.mcfpp.utils

import top.mcfpp.nbt.NbtOps
import top.mcfpp.nbt.tags.CompoundTag
import top.mcfpp.nbt.tags.EndTag
import top.mcfpp.nbt.tags.Tag
import top.mcfpp.nbt.tags.collection.ByteArrayTag
import top.mcfpp.nbt.tags.collection.IntArrayTag
import top.mcfpp.nbt.tags.collection.ListTag
import top.mcfpp.nbt.tags.collection.LongArrayTag

object TagUtils {

    @JvmStatic
    inline fun <reified T> `tag=`(value:T):Tag{
        val ops = NbtOps.INSTANCE
        return when (value){
            is String -> ops.createString(value)
            is Short -> ops.createShort(value)
            is Int -> ops.createInt(value)
            is Long -> ops.createLong(value)
            is Byte -> ops.createByte(value)
            is Boolean -> ops.createBoolean(value)
            is Tag -> value
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
    inline fun <reified T> `array=`(vararg values:T):Tag{
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

}