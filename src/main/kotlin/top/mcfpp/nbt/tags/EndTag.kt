package top.mcfpp.nbt.tags

import top.mcfpp.nbt.visitors.StringTagVisitor
import top.mcfpp.nbt.visitors.TagVisitor

class EndTag private constructor() : Tag<Unit> {

    override val value = Unit

    override fun toString(): String {
        val stringTagVisitor = StringTagVisitor()
        stringTagVisitor.visitEnd(this)
        return stringTagVisitor.build()
    }

    override fun copy(): EndTag {
        return this
    }

    override fun accept(tagVisitor: TagVisitor) {
        tagVisitor.visitEnd(this)
    }

    companion object {
        @JvmField
		val INSTANCE: EndTag = EndTag()
    }
}
