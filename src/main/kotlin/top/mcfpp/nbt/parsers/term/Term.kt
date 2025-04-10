package top.mcfpp.nbt.parsers.term

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.exceptions.CommandSyntaxException
import it.unimi.dsi.fastutil.chars.CharList
import top.mcfpp.nbt.parsers.error.DelayedException
import top.mcfpp.nbt.parsers.error.SuggestionSupplier
import top.mcfpp.nbt.parsers.state.ParseState
import java.util.stream.Collectors
import java.util.stream.Stream

sealed interface Term {
    fun parse(parseState: ParseState, scope: Scope, control: Control): Boolean

    companion object {
        fun <T> marker(atom: Atom<T>, obj: T): Term {
            return Marker(atom, obj)
        }

        @SafeVarargs
        fun sequence(vararg terms: Term): Term {
            return Sequence(terms)
        }

        @SafeVarargs
        fun alternative(vararg terms: Term): Term {
            return Alternative(terms)
        }

        fun optional(term: Term): Term {
            return Maybe(term)
        }

        fun <T> repeated(entry: Entry<T>, atom: Atom<List<T>>): Term {
            return repeated(entry, atom, 0)
        }

        fun <T> repeated(entry: Entry<T>, atom: Atom<List<T>>, i: Int): Term {
            return Repeated(entry, atom, i)
        }

        fun <T> repeatedWithTrailingSeparator(
            entry: Entry<T>,
            atom: Atom<List<T>>,
            term: Term
        ): Term {
            return repeatedWithTrailingSeparator(entry, atom, term, 0)
        }

        fun <T> repeatedWithTrailingSeparator(
            entry: Entry<T>,
            atom: Atom<List<T>>,
            term: Term,
            i: Int
        ): Term {
            return RepeatedWithSeparator(entry, atom, term, i, true)
        }

        fun <T> repeatedWithoutTrailingSeparator(
            entry: Entry<T>,
            atom: Atom<List<T>>,
            term: Term
        ): Term {
            return repeatedWithoutTrailingSeparator(entry, atom, term, 0)
        }

        fun <T> repeatedWithoutTrailingSeparator(
            entry: Entry<T>,
            atom: Atom<List<T>>,
            term: Term,
            i: Int
        ): Term {
            return RepeatedWithSeparator(entry, atom, term, i, false)
        }

        fun positiveLookahead(term: Term): Term {
            return LookAhead(term, true)
        }

        fun negativeLookahead(term: Term): Term {
            return LookAhead(term, false)
        }

        fun cut(): Term {
            return Cut
        }

        fun empty(): Term {
            return Empty
        }

        fun <T> fail(obj: T): Term {
            return Fail(obj)
        }

        fun word(string: String): Term {
            return TerminalWord(string)
        }

        fun character(c: Char): Term {
            return object : TerminalCharacters(CharList.of(c)) {
                override fun isAccepted(c1: Char): Boolean {
                    return c == c1
                }
            }
        }

        fun characters(c: Char, d: Char): Term {
            return object : TerminalCharacters(CharList.of(c, d)) {
                override fun isAccepted(c1: Char): Boolean {
                    return c1 == c || c1 == d
                }
            }
        }

        fun createReader(string: String?, i: Int): StringReader {
            val stringReader = StringReader(string)
            stringReader.cursor = i
            return stringReader
        }

    }

}

data class Fail<T>(val obj:T) : Term {
    override fun parse(parseState: ParseState, scope: Scope, control: Control): Boolean {
        parseState.errorCollector().store(parseState.mark(), obj)
        return false
    }

    override fun toString(): String {
        return "fail"
    }
}

object Empty : Term {
    override fun parse(parseState: ParseState, scope: Scope, control: Control): Boolean {
        return true
    }

    override fun toString(): String {
        return "ε"
    }
}

object Cut : Term {
    override fun parse(parseState: ParseState, scope: Scope, control: Control): Boolean {
        control.cut()
        return true
    }

    override fun toString(): String {
        return "↑"
    }
}


@JvmRecord
data class Alternative(val elements: Array<out Term>) : Term {
    override fun parse(parseState: ParseState, scope: Scope, control: Control): Boolean {
        val control2 = parseState.acquireControl()

        try {
            val i = parseState.mark()
            scope.splitFrame()

            for (term in this.elements) {
                if (term.parse(parseState, scope, control2)) {
                    scope.mergeFrame()
                    return true
                }

                scope.clearFrameValues()
                parseState.restore(i)
                if (control2.hasCut()) {
                    break
                }
            }

            scope.popFrame()
            return false
        } finally {
            parseState.releaseControl()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Alternative

        return elements.contentEquals(other.elements)
    }

    override fun hashCode(): Int {
        return elements.contentHashCode()
    }
}

@JvmRecord
data class LookAhead(val term: Term, val positive: Boolean) : Term {
    override fun parse(parseState: ParseState, scope: Scope, control: Control): Boolean {
        val i = parseState.mark()
        val bl = term.parse(parseState.silent(), scope, control)
        parseState.restore(i)
        return this.positive == bl
    }
}

@JvmRecord
data class Marker<T>(val name: Atom<T>, val value: T) : Term {
    override fun parse(parseState: ParseState, scope: Scope, control: Control): Boolean {
        scope.put(this.name, this.value)
        return true
    }
}

@JvmRecord
data class Maybe(val term: Term) : Term {
    override fun parse(parseState: ParseState, scope: Scope, control: Control): Boolean {
        val i = parseState.mark()
        if (!term.parse(parseState, scope, control)) {
            parseState.restore(i)
        }

        return true
    }
}

@JvmRecord
data class Repeated<T>(val element: Entry<T>, val listName: Atom<List<T>>, val minRepetitions: Int) :
    Term {
    override fun parse(parseState: ParseState, scope: Scope, control: Control): Boolean {
        val i = parseState.mark()
        val list: MutableList<T> = ArrayList(this.minRepetitions)

        while (true) {
            val j = parseState.mark()
            val obj = parseState.parse(this.element)
            if (obj == null) {
                parseState.restore(j)
                if (list.size <this.minRepetitions) {
                    parseState.restore(i)
                    return false
                } else {
                    scope.put(this.listName, list)
                    return true
                }
            }

            list.add(obj)
        }
    }
}

@JvmRecord
data class RepeatedWithSeparator<T>(
    val element: Entry<T>,
    val listName: Atom<List<T>>,
    val separator: Term,
    val minRepetitions: Int,
    val allowTrailingSeparator: Boolean
) : Term {
    override fun parse(parseState: ParseState, scope: Scope, control: Control): Boolean {
        val i = parseState.mark()
        val list: MutableList<T> = ArrayList(this.minRepetitions)
        var bl = true

        while (true) {
            val j = parseState.mark()
            if (!bl && !separator.parse(parseState, scope, control)) {
                parseState.restore(j)
                break
            }

            val k = parseState.mark()
            val obj = parseState.parse(this.element)
            if (obj == null) {
                if (bl) {
                    parseState.restore(k)
                } else {
                    if (!this.allowTrailingSeparator) {
                        parseState.restore(i)
                        return false
                    }

                    parseState.restore(k)
                }
                break
            }

            list.add(obj)
            bl = false
        }

        if (list.size <this.minRepetitions) {
            parseState.restore(i)
            return false
        } else {
            scope.put(this.listName, list)
            return true
        }
    }
}

@JvmRecord
data class Sequence(val elements: Array<out Term>) : Term {
    override fun parse(parseState: ParseState, scope: Scope, control: Control): Boolean {
        val i = parseState.mark()

        for (term in this.elements) {
            if (!term.parse(parseState, scope, control)) {
                parseState.restore(i)
                return false
            }
        }

        return true
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Sequence

        return elements.contentEquals(other.elements)
    }

    override fun hashCode(): Int {
        return elements.contentHashCode()
    }
}

abstract class TerminalCharacters(charList: CharList) : Term {
    private val error: DelayedException<CommandSyntaxException>
    private val suggestions: SuggestionSupplier

    init {
        val string = charList.intStream().mapToObj { codePoint: Int ->
            Character.toString(
                codePoint
            )
        }.collect(Collectors.joining("|"))
        this.error =
            DelayedException.create(CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect(), string.toString())
        this.suggestions =
            SuggestionSupplier { parseState: ParseState ->
                charList.intStream().mapToObj { codePoint: Int ->
                    Character.toString(
                        codePoint
                    )
                }
            }
    }

    override fun parse(parseState: ParseState, scope: Scope, control: Control): Boolean {
        parseState.input().skipWhitespace()
        val i = parseState.mark()
        if (parseState.input().canRead() && this.isAccepted(parseState.input().read())) {
            return true
        } else {
            parseState.errorCollector().store(i, this.suggestions, this.error)
            return false
        }
    }

    protected abstract fun isAccepted(c: Char): Boolean
}

class TerminalWord(private val value: String) : Term {
    private val error: DelayedException<CommandSyntaxException> = DelayedException.create(
        CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect(),
        value
    )
    private val suggestions =
        SuggestionSupplier { parseState: ParseState? ->
            Stream.of(
                value
            )
        }

    override fun parse(parseState: ParseState, scope: Scope, control: Control): Boolean {
        parseState.input().skipWhitespace()
        val i = parseState.mark()
        val string = parseState.input().readUnquotedString()
        if (string != value) {
            parseState.errorCollector().store(i, this.suggestions, this.error)
            return false
        } else {
            return true
        }
    }

    override fun toString(): String {
        return "terminal[" + this.value + "]"
    }
}