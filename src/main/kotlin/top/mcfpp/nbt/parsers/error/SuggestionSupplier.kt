package top.mcfpp.nbt.parsers.error

import top.mcfpp.nbt.parsers.state.ParseState
import java.util.stream.Stream

fun interface SuggestionSupplier {
    fun possibleValues(parseState: ParseState): Stream<String>

    companion object {
        fun empty(): SuggestionSupplier {
            return SuggestionSupplier { Stream.empty() }
        }
    }
}
