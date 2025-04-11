package top.mcfpp.nbt.tags.primitive

import top.mcfpp.nbt.parsers.SnbtGrammarUtils.escapeControlCharacters
import top.mcfpp.nbt.visitors.StringTagVisitor
import top.mcfpp.nbt.visitors.TagVisitor
import java.util.*

@JvmRecord
data class StringTag(val value: String) : PrimitiveTag {
    override fun toString(): String {
        val stringTagVisitor = StringTagVisitor()
        stringTagVisitor.visitString(this)
        return stringTagVisitor.build()
    }

    override fun copy(): StringTag {
        return this
    }

    override fun asString(): Optional<String> {
        return Optional.of(this.value)
    }

    override fun accept(tagVisitor: TagVisitor) {
        tagVisitor.visitString(this)
    }

    companion object {
        private val EMPTY = StringTag("")
        private const val DOUBLE_QUOTE = '"'
        private const val SINGLE_QUOTE = '\''
        private const val ESCAPE = '\\'
        private const val NOT_SET = '\u0000'

        @JvmStatic
		fun valueOf(string: String): StringTag {
            return if (string.isEmpty()) EMPTY else StringTag(string)
        }

        fun quoteAndEscape(string: String): String {
            val stringBuilder = StringBuilder()
            quoteAndEscape(string, stringBuilder)
            return stringBuilder.toString()
        }

        fun quoteAndEscape(string: String, stringBuilder: StringBuilder) {
            val i = stringBuilder.length
            stringBuilder.append(' ')
            var c = 0.toChar()

            for (j in 0 until string.length) {
                val d = string[j]
                if (d == ESCAPE) {
                    stringBuilder.append("\\\\")
                } else if (d != DOUBLE_QUOTE && d != SINGLE_QUOTE) {
                    val string2 = escapeControlCharacters(d)
                    if (string2 != null) {
                        stringBuilder.append(ESCAPE)
                        stringBuilder.append(string2)
                    } else {
                        stringBuilder.append(d)
                    }
                } else {
                    if (c.code == 0) {
                        c = (if (d == DOUBLE_QUOTE) 39 else 34).toChar()
                    }

                    if (c == d) {
                        stringBuilder.append(ESCAPE)
                    }

                    stringBuilder.append(d)
                }
            }

            if (c.code == 0) {
                c = DOUBLE_QUOTE
            }

            stringBuilder.setCharAt(i, c)
            stringBuilder.append(c)
        }
    }
}
