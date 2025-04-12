package top.mcfpp.nbt.parsers

import top.mcfpp.nbt.parsers.error.SnbtException.ERROR_EXPECTED_NUMBER_OR_BOOLEAN
import top.mcfpp.nbt.parsers.error.SnbtException.ERROR_EXPECTED_STRING_UUID
import top.mcfpp.nbt.parsers.error.SuggestionSupplier
import top.mcfpp.nbt.parsers.state.ParseState
import top.mcfpp.nbt.tags.Tag
import top.mcfpp.nbt.tags.primitive.ByteTag.Companion.valueOf
import top.mcfpp.nbt.tags.primitive.StringTag
import top.mcfpp.utils.TagUtils
import top.mcfpp.utils.UUIDUtil
import java.util.*
import java.util.stream.Collectors
import java.util.stream.IntStream
import java.util.stream.Stream

object SnbtOperationsKt {
    const val BUILTIN_TRUE: String = "true"
    const val BUILTIN_FALSE: String = "false"

    val BUILTIN_OPERATIONS: Map<BuiltinKey, BuiltinOperation> = mapOf(
        BuiltinKey("bool", 1) to object : BuiltinOperation {
            override fun run(list: List<Tag<*>>, parseState: ParseState): Tag<*>? {
                val boolean_ = convert(list.first())
                return if (boolean_ == null) {
                    parseState.errorCollector().store(parseState.mark(), ERROR_EXPECTED_NUMBER_OR_BOOLEAN)
                    null
                } else {
                    valueOf(boolean_)
                }
            }

            private fun convert( `object`: Tag<*>): Boolean? {
                val optional = getBooleanValue(`object`)
                return if (optional.isPresent) {
                    optional.get()
                } else {
                    val optional2 = getNumberValue(`object`)
                    if (optional2.isPresent) optional2.get().toDouble() != 0.0 else null
                }
            }
        },
        BuiltinKey("uuid", 1) to object : BuiltinOperation {
            override fun run(list: List<Tag<*>>, parseState: ParseState): Tag<*>? {
                val optional = getStringValue(list.first())
                if (optional.isEmpty) {
                    parseState.errorCollector().store(parseState.mark(), ERROR_EXPECTED_STRING_UUID)
                    return null
                } else {
                    val uUID = try {
                        UUID.fromString(optional.get())
                    } catch (var7: IllegalArgumentException) {
                        parseState.errorCollector().store(parseState.mark(), ERROR_EXPECTED_STRING_UUID)
                        return null
                    }

                    return TagUtils.createIntList(IntStream.of(*UUIDUtil.uuidToIntArray(uUID)))
                }
            }
        }
    )

    fun getNumberValue(tag: Tag<*>): Optional<Number> {
        return tag.asNumber()?.let { Optional.ofNullable(it) }?:  Optional.empty()
    }

    fun getStringValue(tag: Tag<*>): Optional<String> {
        return if (tag is StringTag) Optional.ofNullable(tag.value) else Optional.empty()
    }

    fun getBooleanValue(input: Tag<*>): Optional<Boolean> {
        return getNumberValue(input).map { number: Number ->
            number.toByte().toInt() != 0
        }
    }

    val BUILTIN_IDS: SuggestionSupplier = object : SuggestionSupplier {
        private val keys: Set<String> = Stream.concat(
            Stream.of<String>(BUILTIN_FALSE, BUILTIN_TRUE), BUILTIN_OPERATIONS.keys.stream().map(BuiltinKey::id)
        )
            .collect(Collectors.toSet())

        override fun possibleValues(parseState: ParseState): Stream<String> {
            return keys.stream()
        }
    }

    @JvmRecord
    data class BuiltinKey(val id: String, val argCount: Int) {
        override fun toString(): String {
            return this.id + "/" + this.argCount
        }
    }

    interface BuiltinOperation {
        fun run(list: List<Tag<*>>, parseState: ParseState): Tag<*>?
    }
}
