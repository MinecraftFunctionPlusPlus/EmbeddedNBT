package top.mcfpp.nbt

import com.google.common.annotations.VisibleForTesting
import com.mojang.brigadier.exceptions.CommandSyntaxException
import top.mcfpp.nbt.parsers.Parser.parse
import top.mcfpp.nbt.tags.CompoundTag
import top.mcfpp.nbt.tags.Tag
import top.mcfpp.nbt.tags.collection.ListTag
import top.mcfpp.nbt.visitors.SnbtPrinterTagVisitor
import top.mcfpp.nbt.visitors.SnbtTagVisitor

object NBTUtils {
    @VisibleForTesting
    fun compareNbt(tag: Tag<*>?, tag2: Tag<*>?, bl: Boolean): Boolean {
        if (tag === tag2) {
            return true
        } else if (tag == null) {
            return true
        } else if (tag2 == null) {
            return false
        } else if (tag.javaClass != tag2.javaClass) {
            return false
        } else if (tag is CompoundTag) {
            val compoundTag2 = tag2 as CompoundTag
            if (compoundTag2.size() < tag.size()) {
                return false
            } else {
                for ((key, tag3) in tag.entrySet()) {
                    if (!compareNbt(tag3, compoundTag2[key], bl)) {
                        return false
                    }
                }

                return true
            }
        } else if (tag is ListTag && bl) {
            val listTag2 = tag2 as ListTag
            if (tag.isEmpty()) {
                return listTag2.isEmpty()
            } else if (listTag2.size < tag.size) {
                return false
            } else {
                for (tag4 in tag) {
                    var bl2 = false

                    for (tag5 in listTag2) {
                        if (compareNbt(tag4, tag5, bl)) {
                            bl2 = true
                            break
                        }
                    }

                    if (!bl2) {
                        return false
                    }
                }

                return true
            }
        } else {
            return tag == tag2
        }
    }

    fun toSNBTPretty(compoundTag: CompoundTag?): String {
        return SnbtPrinterTagVisitor().visit(compoundTag)
    }

    fun toSNBT(tag: Tag<*>): String {
        return SnbtTagVisitor().visit(tag)
    }

    @Throws(CommandSyntaxException::class)
    fun toNBT(string: String): Tag<*> {
        return parse(string)
    }
}

