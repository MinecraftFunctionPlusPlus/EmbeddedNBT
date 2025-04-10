package top.mcfpp.nbt.parsers.rules

import com.mojang.brigadier.exceptions.CommandSyntaxException
import top.mcfpp.nbt.parsers.error.DelayedException
import top.mcfpp.nbt.parsers.state.ParseState

abstract class GreedyPredicateParseRule(
    private val minSize: Int,
    private val maxSize: Int,
    private val error: DelayedException<CommandSyntaxException>
) : Rule<String> {
    constructor(i: Int, delayedException: DelayedException<CommandSyntaxException>) : this(
        i,
        Int.MAX_VALUE,
        delayedException
    )

    override fun parse(parseState: ParseState): String? {
        val stringReader = parseState.input()
        val string = stringReader.string
        val i = stringReader.cursor
        var j = i

        while (j < string.length && this.isAccepted(string[j]) && j - i < this.maxSize) {
            j++
        }

        val k = j - i
        if (k < this.minSize) {
            parseState.errorCollector().store(parseState.mark(), this.error)
            return null
        } else {
            stringReader.cursor = j
            return string.substring(i, j)
        }
    }

    protected abstract fun isAccepted(c: Char): Boolean
}
