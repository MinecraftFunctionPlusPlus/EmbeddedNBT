package top.mcfpp.nbt.parsers

import it.unimi.dsi.fastutil.chars.CharList
import top.mcfpp.nbt.parsers.rules.GreedyPredicateParseRule
import top.mcfpp.nbt.parsers.rules.NumberRunParseRule
import top.mcfpp.nbt.parsers.state.ParseState
import top.mcfpp.nbt.parsers.error.SnbtException
import top.mcfpp.nbt.parsers.term.TerminalCharacters
import top.mcfpp.nbt.tags.Tag
import top.mcfpp.nbt.tags.primitive.DoubleTag.Companion.valueOf
import top.mcfpp.nbt.tags.primitive.FloatTag.Companion.valueOf
import java.util.*
import java.util.regex.Pattern

@Suppress("UNCHECKED_CAST")
object SnbtGrammarUtils {
    @JvmField
    val HEX_ESCAPE: HexFormat = HexFormat.of().withUpperCase()
    @JvmField
    val BINARY_NUMERAL: NumberRunParseRule = object :
        NumberRunParseRule(SnbtException.ERROR_EXPECTED_BINARY_NUMERAL, SnbtException.ERROR_UNDESCORE_NOT_ALLOWED) {
        override fun isAccepted(c: Char): Boolean {
            return when (c) {
                '0', '1', '_' -> true
                else -> false
            }
        }
    }
    @JvmField
    val DECIMAL_NUMERAL: NumberRunParseRule = object :
        NumberRunParseRule(SnbtException.ERROR_EXPECTED_DECIMAL_NUMERAL, SnbtException.ERROR_UNDESCORE_NOT_ALLOWED) {
        override fun isAccepted(c: Char): Boolean {
            return when (c) {
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '_' -> true
                else -> false
            }
        }
    }
    @JvmField
    val HEX_NUMERAL: NumberRunParseRule = object :
        NumberRunParseRule(SnbtException.ERROR_EXPECTED_HEX_NUMERAL, SnbtException.ERROR_UNDESCORE_NOT_ALLOWED) {
        override fun isAccepted(c: Char): Boolean {
            return when (c) {
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', '_', 'a', 'b', 'c', 'd', 'e', 'f' -> true
                else -> false
            }
        }
    }
    @JvmField
    val PLAIN_STRING_CHUNK: GreedyPredicateParseRule =
        object : GreedyPredicateParseRule(1, SnbtException.ERROR_INVALID_STRING_CONTENTS) {
            override fun isAccepted(c: Char): Boolean {
                return when (c) {
                    '"', '\'', '\\' -> false
                    else -> true
                }
            }
        }
    @JvmField
    val NUMBER_LOOKEAHEAD: TerminalCharacters =
        object : TerminalCharacters(CharList.of()) {
            override fun isAccepted(c: Char): Boolean {
                return canStartNumber(c)
            }
        }
    @JvmField
    val UNICODE_NAME: Pattern = Pattern.compile("[-a-zA-Z0-9 ]+")


    @JvmStatic
    fun escapeControlCharacters(c: Char): String? {
        return when (c) {
            '\b' -> "b"
            '\t' -> "t"
            '\n' -> "n"
            '\u000C' -> "f"
            '\r' -> "r"
            else -> {
                if (c < ' ') "x" + HEX_ESCAPE.toHexDigits(c.code.toByte()) else null
            }
        }
    }

    @JvmStatic
    fun isAllowedToStartUnquotedString(c: Char): Boolean {
        return !canStartNumber(c)
    }

    @JvmStatic
    fun canStartNumber(c: Char): Boolean {
        return when (c) {
            '+', '-', '.', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> true
            else -> false
        }
    }

    @JvmStatic
    fun needsUnderscoreRemoval(string: String): Boolean {
        return string.indexOf(95.toChar()) != -1
    }

    @JvmOverloads
    fun cleanAndAppend(stringBuilder: StringBuilder, string: String, bl: Boolean = needsUnderscoreRemoval(string)) {
        if (bl) {
            for (c in string.toCharArray()) {
                if (c != '_') {
                    stringBuilder.append(c)
                }
            }
        } else {
            stringBuilder.append(string)
        }
    }

    @JvmStatic
    fun parseUnsignedShort(string: String, i: Int): Short {
        val j = string.toInt(i)
        if (j shr 16 == 0) {
            return j.toShort()
        } else {
            throw NumberFormatException("out of range: $j")
        }
    }

    @JvmStatic
    fun  createFloat(
        sign: Sign,
        string: String?,
        string2: String?,
        signed: Signed<String>?,
        typeSuffix: TypeSuffix?,
        parseState: ParseState
    ): Tag<*>? {
        val stringBuilder = StringBuilder()
        sign.append(stringBuilder)
        if (string != null) {
            cleanAndAppend(stringBuilder, string)
        }

        if (string2 != null) {
            stringBuilder.append('.')
            cleanAndAppend(stringBuilder, string2)
        }

        if (signed != null) {
            stringBuilder.append('e')
            signed.sign.append(stringBuilder)
            cleanAndAppend(stringBuilder, signed.value)
        }

        try {
            val string3 = stringBuilder.toString()

            return when (typeSuffix) {
                null -> convertDouble( parseState, string3)
                TypeSuffix.FLOAT -> convertFloat( parseState, string3)
                TypeSuffix.DOUBLE -> convertDouble( parseState, string3)
                else -> {
                    parseState.errorCollector().store(parseState.mark(), SnbtException.ERROR_EXPECTED_FLOAT_TYPE)
                    null
                }
            } as Tag<*>
        } catch (var11: NumberFormatException) {
            parseState.errorCollector().store(parseState.mark(), SnbtException.createNumberParseError(var11))
            return null
        }
    }

    @JvmStatic
    fun convertFloat( parseState: ParseState, string: String): Tag<*>? {
        val f = string.toFloat()
        if (!java.lang.Float.isFinite(f)) {
            parseState.errorCollector().store(parseState.mark(), SnbtException.ERROR_INFINITY_NOT_ALLOWED)
            return null
        } else {
            return valueOf(f)
        }
    }

    @JvmStatic
    fun  convertDouble( parseState: ParseState, string: String): Tag<*>? {
        val d = string.toDouble()
        if (!java.lang.Double.isFinite(d)) {
            parseState.errorCollector().store(parseState.mark(), SnbtException.ERROR_INFINITY_NOT_ALLOWED)
            return null
        } else {
            return valueOf(d)
        }
    }

    fun joinList(list: List<String?>): String {
        return when (list.size) {
            0 -> ""
            1 -> list.first()!!
            else -> java.lang.String.join("", list)
        }
    }
}
