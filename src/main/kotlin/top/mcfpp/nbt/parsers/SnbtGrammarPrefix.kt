package top.mcfpp.nbt.parsers

import com.google.common.primitives.UnsignedBytes
import it.unimi.dsi.fastutil.bytes.ByteArrayList
import it.unimi.dsi.fastutil.bytes.ByteList
import top.mcfpp.nbt.parsers.error.SnbtException
import top.mcfpp.nbt.parsers.state.ParseState
import top.mcfpp.nbt.tags.Tag
import top.mcfpp.nbt.tags.primitive.ByteTag.Companion.valueOf
import top.mcfpp.nbt.tags.primitive.IntTag.Companion.valueOf
import top.mcfpp.nbt.tags.primitive.LongTag.Companion.valueOf
import top.mcfpp.nbt.tags.primitive.ShortTag.Companion.valueOf
import top.mcfpp.utils.TagUtils
import java.nio.ByteBuffer
import java.util.*
import java.util.stream.IntStream
import java.util.stream.LongStream



enum class ArrayPrefix(private val defaultType: TypeSuffix, vararg typeSuffixs: TypeSuffix) {

    BYTE(TypeSuffix.BYTE) {
        override fun create(): Tag<*> {
            return TagUtils.createByteList(EMPTY_BUFFER)
        }

        override fun  create(
            list: List<IntegerLiteral?>,
            parseState: ParseState?
        ): Tag<*>? {
            val byteList: ByteList = ByteArrayList()

            for (integerLiteral in list) {
                val number = this.buildNumber(integerLiteral!!, parseState!!) ?: return null

                byteList.add(number.toByte())
            }

            return TagUtils.createByteList(ByteBuffer.wrap(byteList.toByteArray()))
        }
    },
    INT(TypeSuffix.INT, TypeSuffix.BYTE, TypeSuffix.SHORT) {
        override fun create(): Tag<*> {
            return TagUtils.createIntList(IntStream.empty())
        }

        override fun  create(
            list: List<IntegerLiteral?>,
            parseState: ParseState?
        ): Tag<*>? {
            val builder = IntStream.builder()

            for (integerLiteral in list) {
                val number = this.buildNumber(integerLiteral!!, parseState!!) ?: return null

                builder.add(number.toInt())
            }

            return TagUtils.createIntList(builder.build())
        }
    },
    LONG(TypeSuffix.LONG, TypeSuffix.BYTE, TypeSuffix.SHORT, TypeSuffix.INT) {
        override fun create(): Tag<*> {
            return TagUtils.createLongList(LongStream.empty())
        }

        override fun create(
            list: List<IntegerLiteral?>,
            parseState: ParseState?
        ): Tag<*>? {
            val builder = LongStream.builder()

            for (integerLiteral in list) {
                val number = this.buildNumber(integerLiteral!!, parseState!!) ?: return null

                builder.add(number.toLong())
            }

            return TagUtils.createLongList(builder.build())
        }
    };

    private val additionalTypes: Set<TypeSuffix> = java.util.Set.of(*typeSuffixs)

    fun isAllowed(typeSuffix: TypeSuffix): Boolean {
        return typeSuffix == this.defaultType || additionalTypes.contains(typeSuffix)
    }

    abstract fun  create(): Tag<*>

    abstract fun  create( list: List<IntegerLiteral?>, parseState: ParseState?): Tag<*>?

    protected fun buildNumber(integerLiteral: IntegerLiteral, parseState: ParseState): Number? {
        val typeSuffix = this.computeType(integerLiteral.suffix)
        if (typeSuffix == null) {
            parseState.errorCollector().store(parseState.mark(), SnbtException.ERROR_INVALID_ARRAY_ELEMENT_TYPE)
            return null
        } else {
            return integerLiteral.create(typeSuffix, parseState)?.asNumber()
        }
    }

    private fun computeType(integerSuffix: IntegerSuffix): TypeSuffix? {
        val typeSuffix = integerSuffix.type
        return if (typeSuffix == null) {
            defaultType
        } else {
            if (!this.isAllowed(typeSuffix)) null else typeSuffix
        }
    }

    companion object{
        private val EMPTY_BUFFER: ByteBuffer = ByteBuffer.wrap(ByteArray(0))
    }
}

enum class Base {
    BINARY,
    DECIMAL,
    HEX
}

@Suppress("UNCHECKED_CAST")
@JvmRecord
data class IntegerLiteral(val sign: Sign, val base: Base, val digits: String, val suffix: IntegerSuffix) {
    private fun signedOrDefault(): SignedPrefix {
        return suffix.signed
            ?: when (this.base) {
                Base.BINARY, Base.HEX -> SignedPrefix.UNSIGNED
                Base.DECIMAL -> SignedPrefix.SIGNED
            }
    }

    private fun cleanupDigits(sign: Sign): String {
        val bl = SnbtGrammarUtils.needsUnderscoreRemoval(this.digits)
        if (sign != Sign.MINUS && !bl) {
            return this.digits
        } else {
            val stringBuilder = StringBuilder()
            sign.append(stringBuilder)
            SnbtGrammarUtils.cleanAndAppend(stringBuilder, this.digits, bl)
            return stringBuilder.toString()
        }
    }

    fun create(parseState: ParseState): Tag<*>? {
        return this.create( Objects.requireNonNullElse(suffix.type, TypeSuffix.INT), parseState)
    }

    fun create(typeSuffix: TypeSuffix?, parseState: ParseState): Tag<*>? {
        val bl = this.signedOrDefault() == SignedPrefix.SIGNED
        if (!bl && this.sign == Sign.MINUS) {
            parseState.errorCollector().store(parseState.mark(), SnbtException.ERROR_EXPECTED_NON_NEGATIVE_NUMBER)
            return null
        } else {
            val string = this.cleanupDigits(this.sign)

            val i = when (this.base) {
                Base.BINARY -> 2
                Base.DECIMAL -> 10
                Base.HEX -> 16
            }

            try {
                return if (bl) {
                    when (typeSuffix) {
                        TypeSuffix.BYTE -> valueOf(string.toByte(i))
                        TypeSuffix.SHORT -> valueOf(string.toShort(i))
                        TypeSuffix.INT -> valueOf(string.toInt(i))
                        TypeSuffix.LONG -> valueOf(string.toLong(i))
                        else -> {
                            parseState.errorCollector()
                                .store(parseState.mark(), SnbtException.ERROR_EXPECTED_INTEGER_TYPE)
                            null
                        }
                    }
                } else {
                    when (typeSuffix) {
                        TypeSuffix.BYTE -> valueOf(UnsignedBytes.parseUnsignedByte(string, i))
                        TypeSuffix.SHORT -> valueOf(SnbtGrammarUtils.parseUnsignedShort(string, i))
                        TypeSuffix.INT -> valueOf(Integer.parseUnsignedInt(string, i))
                        TypeSuffix.LONG -> valueOf(java.lang.Long.parseUnsignedLong(string, i))
                        else -> {
                            parseState.errorCollector()
                                .store(parseState.mark(), SnbtException.ERROR_EXPECTED_INTEGER_TYPE)
                            null
                        }
                    }
                }
            } catch (var8: NumberFormatException) {
                parseState.errorCollector().store(parseState.mark(), SnbtException.createNumberParseError(var8))
                return null
            }
        }
    }
}

@JvmRecord
data class IntegerSuffix(val signed: SignedPrefix?, val type: TypeSuffix?) {
    companion object {
        val EMPTY: IntegerSuffix = IntegerSuffix(null, null)
    }
}

enum class Sign {
    PLUS,
    MINUS;

    fun append(stringBuilder: StringBuilder) {
        if (this == MINUS) {
            stringBuilder.append("-")
        }
    }
}

@JvmRecord
data class Signed<T>(val sign: Sign, val value: T)

enum class SignedPrefix {
    SIGNED,
    UNSIGNED
}


enum class TypeSuffix {
    FLOAT,
    DOUBLE,
    BYTE,
    SHORT,
    INT,
    LONG
}