package top.mcfpp.nbt.tags.primitive

import top.mcfpp.nbt.tags.Tag

interface PrimitiveTag<V> : Tag<V> {
    override fun copy(): Tag<V> {
        return this
    }
}
