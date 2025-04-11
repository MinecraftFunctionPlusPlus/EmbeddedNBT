package top.mcfpp.nbt.tags.primitive

import top.mcfpp.nbt.tags.Tag

interface PrimitiveTag : Tag {
    override fun copy(): Tag {
        return this
    }
}
