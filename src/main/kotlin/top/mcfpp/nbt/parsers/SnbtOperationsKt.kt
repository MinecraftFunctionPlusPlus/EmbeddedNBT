package top.mcfpp.nbt.parsers

import com.mojang.serialization.DynamicOps
import top.mcfpp.nbt.parsers.error.SnbtException.ERROR_EXPECTED_NUMBER_OR_BOOLEAN
import top.mcfpp.nbt.parsers.error.SnbtException.ERROR_EXPECTED_STRING_UUID
import top.mcfpp.nbt.parsers.error.SuggestionSupplier
import top.mcfpp.nbt.parsers.state.ParseState
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
            override fun <T> run(dynamicOps: DynamicOps<T>, list: List<T>, parseState: ParseState): T? {
                val boolean_ = convert(dynamicOps, list.first())
                return if (boolean_ == null) {
                    parseState.errorCollector().store(parseState.mark(), ERROR_EXPECTED_NUMBER_OR_BOOLEAN)
                    null
                } else {
                    dynamicOps.createBoolean(boolean_)
                }
            }

            private fun <T> convert(dynamicOps: DynamicOps<T>, `object`: T): Boolean? {
                val optional = dynamicOps.getBooleanValue(`object`).result()
                return if (optional.isPresent) {
                    optional.get()
                } else {
                    val optional2 = dynamicOps.getNumberValue(`object`).result()
                    if (optional2.isPresent) optional2.get().toDouble() != 0.0 else null
                }
            }
        },
        BuiltinKey("uuid", 1) to object : BuiltinOperation {
            override fun <T> run(dynamicOps: DynamicOps<T>, list: List<T>, parseState: ParseState): T? {
                val optional = dynamicOps.getStringValue(list.first()).result()
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

                    return dynamicOps.createIntList(IntStream.of(*UUIDUtil.uuidToIntArray(uUID)))
                }
            }
        }
    )

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
        fun <T> run(dynamicOps: DynamicOps<T>, list: List<T>, parseState: ParseState): T?
    }
}
