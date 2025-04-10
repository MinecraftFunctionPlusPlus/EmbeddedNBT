package top.mcfpp.nbt.parsers.rules

import com.mojang.brigadier.exceptions.CommandSyntaxException
import top.mcfpp.nbt.parsers.error.DelayedException
import top.mcfpp.nbt.parsers.state.ParseState
import java.util.regex.Pattern

class GreedyPatternParseRule(
    private val pattern: Pattern,
    private val error: DelayedException<CommandSyntaxException>
) : Rule<String> {
    override fun parse(parseState: ParseState): String? {
        val stringReader = parseState.input()
        val string = stringReader.string
        val matcher = pattern.matcher(string).region(stringReader.cursor, string.length)
        if (!matcher.lookingAt()) {
            parseState.errorCollector().store(parseState.mark(), this.error)
            return null
        } else {
            stringReader.cursor = matcher.end()
            return matcher.group(0)
        }
    }
}
