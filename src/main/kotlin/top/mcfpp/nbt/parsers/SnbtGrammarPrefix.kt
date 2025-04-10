package top.mcfpp.nbt.parsers

import com.google.common.primitives.UnsignedBytes
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.JavaOps
import it.unimi.dsi.fastutil.bytes.ByteArrayList
import it.unimi.dsi.fastutil.bytes.ByteList
import top.mcfpp.nbt.parsers.error.SnbtException
import top.mcfpp.nbt.parsers.state.ParseState
import java.nio.ByteBuffer
import java.util.*
import java.util.stream.IntStream
import java.util.stream.LongStream



enum class ArrayPrefix(private val defaultType: TypeSuffix, vararg typeSuffixs: TypeSuffix) {

    BYTE(TypeSuffix.BYTE) {
        override fun <T> create(dynamicOps: DynamicOps<T>): T {
            return dynamicOps.createByteList(EMPTY_BUFFER)
        }

        override fun <T> create(
            dynamicOps: DynamicOps<T>,
            list: List<IntegerLiteral?>,
            parseState: ParseState?
        ): T? {
            val byteList: ByteList = ByteArrayList()

            for (integerLiteral in list) {
                val number = this.buildNumber(integerLiteral!!, parseState!!) ?: return null

                byteList.add(number.toByte())
            }

            return dynamicOps.createByteList(ByteBuffer.wrap(byteList.toByteArray()))
        }
    },
    INT(TypeSuffix.INT, TypeSuffix.BYTE, TypeSuffix.SHORT) {
        override fun <T> create(dynamicOps: DynamicOps<T>): T {
            return dynamicOps.createIntList(IntStream.empty())
        }

        override fun <T> create(
            dynamicOps: DynamicOps<T>,
            list: List<IntegerLiteral?>,
            parseState: ParseState?
        ): T? {
            val builder = IntStream.builder()

            for (integerLiteral in list) {
                val number = this.buildNumber(integerLiteral!!, parseState!!) ?: return null

                builder.add(number.toInt())
            }

            return dynamicOps.createIntList(builder.build())
        }
    },
    LONG(TypeSuffix.LONG, TypeSuffix.BYTE, TypeSuffix.SHORT, TypeSuffix.INT) {
        override fun <T> create(dynamicOps: DynamicOps<T>): T {
            return dynamicOps.createLongList(LongStream.empty())
        }

        override fun <T> create(
            dynamicOps: DynamicOps<T>,
            list: List<IntegerLiteral?>,
            parseState: ParseState?
        ): T? {
            val builder = LongStream.builder()

            for (integerLiteral in list) {
                val number = this.buildNumber(integerLiteral!!, parseState!!) ?: return null

                builder.add(number.toLong())
            }

            return dynamicOps.createLongList(builder.build())
        }
    };

    private val additionalTypes: Set<TypeSuffix> = java.util.Set.of(*typeSuffixs)

    fun isAllowed(typeSuffix: TypeSuffix): Boolean {
        return typeSuffix == this.defaultType || additionalTypes.contains(typeSuffix)
    }

    abstract fun <T> create(dynamicOps: DynamicOps<T>): T

    abstract fun <T> create(dynamicOps: DynamicOps<T>, list: List<IntegerLiteral?>, parseState: ParseState?): T?

    protected fun buildNumber(integerLiteral: IntegerLiteral, parseState: ParseState): Number? {
        val typeSuffix = this.computeType(integerLiteral.suffix)
        if (typeSuffix == null) {
            parseState.errorCollector().store(parseState.mark(), SnbtException.ERROR_INVALID_ARRAY_ELEMENT_TYPE)
            return null
        } else {
            return integerLiteral.create(JavaOps.INSTANCE, typeSuffix, parseState) as Number?
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

    fun <T> create(dynamicOps: DynamicOps<T>, parseState: ParseState): T? {
        return this.create(dynamicOps, Objects.requireNonNullElse(suffix.type, TypeSuffix.INT), parseState)
    }

    fun <T> create(dynamicOps: DynamicOps<T>, typeSuffix: TypeSuffix?, parseState: ParseState): T? {
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
                        TypeSuffix.BYTE -> dynamicOps.createByte(string.toByte(i)) as Any
                        TypeSuffix.SHORT -> dynamicOps.createShort(string.toShort(i)) as Any
                        TypeSuffix.INT -> dynamicOps.createInt(string.toInt(i)) as Any
                        TypeSuffix.LONG -> dynamicOps.createLong(string.toLong(i)) as Any
                        else -> {
                            parseState.errorCollector()
                                .store(parseState.mark(), SnbtException.ERROR_EXPECTED_INTEGER_TYPE)
                            null
                        }
                    } as T
                } else {
                    when (typeSuffix) {
                        TypeSuffix.BYTE -> dynamicOps.createByte(UnsignedBytes.parseUnsignedByte(string, i)) as Any
                        TypeSuffix.SHORT -> dynamicOps.createShort(
                            SnbtGrammarUtils.parseUnsignedShort(
                                string,
                                i
                            )
                        ) as Any

                        TypeSuffix.INT -> dynamicOps.createInt(Integer.parseUnsignedInt(string, i)) as Any
                        TypeSuffix.LONG -> dynamicOps.createLong(java.lang.Long.parseUnsignedLong(string, i)) as Any
                        else -> {
                            parseState.errorCollector()
                                .store(parseState.mark(), SnbtException.ERROR_EXPECTED_INTEGER_TYPE)
                            null
                        }
                    } as T
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