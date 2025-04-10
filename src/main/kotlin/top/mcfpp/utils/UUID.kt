package top.mcfpp.utils

import java.util.*

object UUIDUtil{
    @JvmStatic
    fun uuidToIntArray(uUID:UUID): IntArray {
        val l = uUID.mostSignificantBits
        val m = uUID.leastSignificantBits
        return leastMostToIntArray(l, m)
    }
}


private fun leastMostToIntArray(l: Long, m: Long): IntArray {
    return intArrayOf((l shr 32).toInt(), l.toInt(), (m shr 32).toInt(), m.toInt())
}