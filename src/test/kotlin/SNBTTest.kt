import org.junit.jupiter.api.Test
import top.mcfpp.nbt.*
import top.mcfpp.nbt.tags.CompoundTag
import top.mcfpp.nbt.tags.collection.ListTag
import top.mcfpp.nbt.tags.primitive.IntTag
import top.mcfpp.utils.TagUtils.`array=`
import top.mcfpp.utils.TagUtils.`compound=`
import top.mcfpp.utils.TagUtils.`list=`


class SNBTTest {

    @Test
    fun testNBTtoSNBT(){
        val tag = CompoundTag().apply {
            put("tag","string")
            put("value_int",1)
            put("value_short",1)
            put("value_byte",1)
            put("value_float",1.0f)
            put("value_double",1.0)
            put("value_bool",true)
            put("array_long", longArrayOf(1,2,3,4))
            put("array_int", intArrayOf(1,2,3,4))
            put("array_byte", byteArrayOf(1,2,3,4))
            put("list", ListTag().apply {
                add(IntTag.valueOf(1))
                add(IntTag.valueOf(2))
                add(IntTag.valueOf(3))
                add(IntTag.valueOf(4))
            })
        }
        val str = NBTUtils.toSNBT(tag)
        println(str)
    }

    @Test
    fun testNBTUtil(){
        val tag1 = `compound=` (
            "tag" to "string",
            "value_bool" to true,
            "value_int" to 1,
            "array_byte" to `array=`(byteArrayOf(1,2,3,4)),
            "array_int" to `array=`(1,2,3,4),
            "array_long" to `array=`(1L,2L,3L,4L),
            "list" to `list=`(1,2L,"string")
            )
        println(tag1)
    }

    @Test
    fun testSNBTtoNBT(){
        val tag = NBTUtils.toNBT("""
            {
                array_byte: [B; 1B, 2B, 3B, 4B],
                array_int: [I; 1, 2, 3, 4],
                array_long: [L; 1L, 2L, 3L, 4L],
                list: [
                    1,
                    2,
                    3,
                    4
                ],
                tag: "string",
                value_bool: 1b,
                value_byte: 1b,
                value_double: 1.0d,
                value_float: 1.0f,
                value_int: 1,
                value_short: 1s
            }
        """.trimIndent())
        println(tag)
    }

}