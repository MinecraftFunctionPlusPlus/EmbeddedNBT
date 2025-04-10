package top.mcfpp.nbt.parsers.rules

import top.mcfpp.nbt.parsers.error.SnbtException
import top.mcfpp.nbt.parsers.error.DelayedException

class SimpleHexLiteralParseRule(i: Int) :
    GreedyPredicateParseRule(i, i, DelayedException.create(SnbtException.ERROR_EXPECTED_HEX_ESCAPE, i.toString())) {
    override fun isAccepted(c: Char): Boolean {
        return when (c) {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'a', 'b', 'c', 'd', 'e', 'f' -> true
            else -> false
        }
    }
}