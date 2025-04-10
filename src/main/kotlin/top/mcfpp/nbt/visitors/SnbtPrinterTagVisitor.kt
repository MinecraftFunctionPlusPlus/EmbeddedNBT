package top.mcfpp.nbt.visitors

import com.google.common.base.Strings
import com.google.common.collect.Lists
import com.google.common.collect.Maps
import com.google.common.collect.Sets
import top.mcfpp.nbt.tags.CompoundTag
import top.mcfpp.nbt.tags.EndTag
import top.mcfpp.nbt.tags.Tag
import top.mcfpp.nbt.tags.collection.ByteArrayTag
import top.mcfpp.nbt.tags.collection.IntArrayTag
import top.mcfpp.nbt.tags.collection.ListTag
import top.mcfpp.nbt.tags.collection.LongArrayTag
import top.mcfpp.nbt.tags.primitive.*
import top.mcfpp.utils.Functional.make
import java.util.*
import java.util.regex.Pattern

open class SnbtPrinterTagVisitor @JvmOverloads constructor(
    private val indentation: String = "    ",
    private val depth: Int = 0,
    private val path: MutableList<String?> = Lists.newArrayList()
) : SnbtTagVisitor()  {
    private var result = ""

    override fun visit(tag: Tag?): String {
        tag!!.accept(this)
        return this.result
    }

    override fun visitList(listTag: ListTag) {
        val warpedListTag = listTag.wrappedList
        if (warpedListTag.isEmpty) {
            this.result = "[]"
        } else {
            val stringBuilder = StringBuilder(LIST_OPEN)
            this.pushPath("[]")
            val string = if (NO_INDENTATION.contains(this.pathString())) "" else this.indentation
            if (!string.isEmpty()) {
                stringBuilder.append(NEWLINE)
            }

            for (i in warpedListTag.indices) {
                stringBuilder.append(Strings.repeat(string, this.depth + 1))
                stringBuilder.append(
                    SnbtPrinterTagVisitor(string, this.depth + 1, this.path).visit(
                        warpedListTag[i]
                    )
                )
                if (i != warpedListTag.size - 1) {
                    stringBuilder.append(ELEMENT_SEPARATOR).append(if (string.isEmpty()) ELEMENT_SPACING else NEWLINE)
                }
            }

            if (!string.isEmpty()) {
                stringBuilder.append(NEWLINE).append(Strings.repeat(string, this.depth))
            }

            stringBuilder.append(LIST_CLOSE)
            this.result = stringBuilder.toString()
            this.popPath()
        }
    }

    override fun visitCompound(compoundTag: CompoundTag) {
        if (compoundTag.isEmpty) {
            this.result = "{}"
        } else {
            val stringBuilder = StringBuilder(STRUCT_OPEN)
            this.pushPath("{}")
            val string = if (NO_INDENTATION.contains(this.pathString())) "" else this.indentation
            if (!string.isEmpty()) {
                stringBuilder.append(NEWLINE)
            }

            val collection: Collection<String> = this.getKeys(compoundTag)
            val iterator = collection.iterator()

            while (iterator.hasNext()) {
                val string2 = iterator.next()
                val tag = compoundTag[string2]
                this.pushPath(string2)
                stringBuilder.append(Strings.repeat(string, this.depth + 1))
                    .append(handleEscapePretty(string2))
                    .append(NAME_VALUE_SEPARATOR)
                    .append(ELEMENT_SPACING)
                    .append(SnbtPrinterTagVisitor(string, this.depth + 1, this.path).visit(tag))
                this.popPath()
                if (iterator.hasNext()) {
                    stringBuilder.append(ELEMENT_SEPARATOR).append(if (string.isEmpty()) ELEMENT_SPACING else NEWLINE)
                }
            }

            if (!string.isEmpty()) {
                stringBuilder.append(NEWLINE).append(Strings.repeat(string, this.depth))
            }

            stringBuilder.append(STRUCT_CLOSE)
            this.result = stringBuilder.toString()
            this.popPath()
        }
    }

    private fun popPath() {
        path.removeLast()
    }

    private fun pushPath(string: String) {
        path.add(string)
    }

    protected fun getKeys(compoundTag: CompoundTag): List<String> {
        val set: MutableSet<String> = Sets.newHashSet(compoundTag.keySet())
        val list: MutableList<String> = Lists.newArrayList()
        val list2 = KEY_ORDER[pathString()]
        if (list2 != null) {
            for (string in list2) {
                if (set.remove(string)) {
                    list.add(string)
                }
            }

            if (!set.isEmpty()) {
                set.stream().sorted().forEach { e: String -> list.add(e) }
            }
        } else {
            list.addAll(set)
            Collections.sort(list)
        }

        return list
    }

    fun pathString(): String {
        return java.lang.String.join(".", this.path)
    }


    companion object {
        private val KEY_ORDER: Map<String, List<String>> =
            make(Maps.newHashMap()) { hashMap: HashMap<String, List<String>> ->
                hashMap["{}"] =
                    Lists.newArrayList("DataVersion", "author", "size", "data", "entities", "palette", "palettes")
                hashMap["{}.data.[].{}"] = Lists.newArrayList("pos", "state", "nbt")
                hashMap["{}.entities.[].{}"] = Lists.newArrayList("blockPos", "pos")
            }
        private val NO_INDENTATION: Set<String> =
            Sets.newHashSet("{}.size.[]", "{}.data.[].{}", "{}.palette.[].{}", "{}.entities.[].{}")
        private val SIMPLE_VALUE: Pattern = Pattern.compile("[A-Za-z0-9._+-]+")

        protected fun handleEscapePretty(string: String): String {
            return if (SIMPLE_VALUE.matcher(string).matches()) string else StringTag.quoteAndEscape(string)
        }
    }
}
