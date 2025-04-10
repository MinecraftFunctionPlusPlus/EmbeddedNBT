package top.mcfpp.nbt.parsers.rules

import com.mojang.brigadier.exceptions.CommandSyntaxException
import top.mcfpp.nbt.parsers.error.DelayedException
import top.mcfpp.nbt.parsers.state.ParseState

class UnquotedStringParseRule(private val minSize: Int, private val error: DelayedException<CommandSyntaxException>) :
    Rule<String> {
    override fun parse(parseState: ParseState): String? {
        parseState.input().skipWhitespace()
        val i = parseState.mark()
        val string = parseState.input().readUnquotedString()
        if (string.length < this.minSize) {
            parseState.errorCollector().store(i, this.error)
            return null
        } else {
            return string
        }
    }
}
