package top.mcfpp.utils

import com.google.common.collect.Lists
import java.util.function.Consumer
import java.util.function.Supplier
import java.util.stream.Collector
import java.util.stream.Collectors

object Functional{
    @JvmStatic
    fun <T> make(supplier: Supplier<T>): T {
        return supplier.get()
    }

    @JvmStatic
    fun <T> make(`object`: T, consumer: Consumer<in T>): T {
        consumer.accept(`object`)
        return `object`
    }

    @JvmStatic
    fun <T> toMutableList(): Collector<T, *, List<T>> {
        return Collectors.toCollection { Lists.newArrayList() }
    }
}