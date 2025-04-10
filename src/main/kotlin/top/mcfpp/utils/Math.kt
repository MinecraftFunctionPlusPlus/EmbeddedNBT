package top.mcfpp.utils

import kotlin.math.max
import kotlin.math.min

object Math{
    @JvmStatic
    fun floor(d: Double): Int {
        val i = d.toInt()
        return if (d < i) i - 1 else i
    }


    @JvmStatic
        /**
         * 取max(3/2*i,j)
         */
    fun growByHalf(i: Int, j: Int): Int {
        return max(min((i.toLong() + (i shr 1)).toDouble(), 2147483639.0), j.toDouble())
            .toInt()
    }

}
