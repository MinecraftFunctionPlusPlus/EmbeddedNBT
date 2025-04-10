package top.mcfpp.nbt.parsers.rules

import com.mojang.brigadier.exceptions.CommandSyntaxException
import top.mcfpp.nbt.parsers.error.DelayedException
import top.mcfpp.nbt.parsers.state.ParseState

abstract class NumberRunParseRule(
    private val noValueError: DelayedException<CommandSyntaxException>,
    private val underscoreNotAllowedError: DelayedException<CommandSyntaxException>
) : Rule<String> {
    override fun parse(parseState: ParseState): String? {
        val stringReader = parseState.input()
        stringReader.skipWhitespace()
        val string = stringReader.string
        val i = stringReader.cursor
        var j = i

        while (j < string.length && this.isAccepted(string[j])) {
            j++
        }

        val k = j - i
        if (k == 0) {
            parseState.errorCollector().store(parseState.mark(), this.noValueError)
            return null
        } else if (string[i] != '_' && string[j - 1] != '_') {
            stringReader.cursor = j
            return string.substring(i, j)
        } else {
            parseState.errorCollector().store(parseState.mark(), this.underscoreNotAllowedError)
            return null
        }
    }

    protected abstract fun isAccepted(c: Char): Boolean
}
