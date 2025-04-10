@file:Suppress("unused")

package top.mcfpp.nbt.parsers.term

import top.mcfpp.nbt.parsers.rules.Rule
import top.mcfpp.nbt.parsers.rules.Rule.SimpleRuleAction
import top.mcfpp.nbt.parsers.state.ParseState
import java.util.*

@Suppress("UNCHECKED_CAST")
class Dictionary {
    private val terms: MutableMap<Atom<*>, Entry<*>> = IdentityHashMap()

    fun <T> put(atom: Atom<T>, rule: Rule<T>): Entry<T> {
        val entry = terms.computeIfAbsent(atom) {  Entry(it) } as Entry<T>
        require(entry.value == null) { "Trying to override rule: $atom" }
        entry.value = rule
        return entry
    }

    fun <T : Any> putComplex(atom: Atom<T>, term: Term, ruleAction: Rule.RuleAction<T>): Entry<T> {
        return this.put(atom, Rule.fromTerm(term, ruleAction))
    }

    fun <T : Any> put(atom: Atom<T>, term: Term, simpleRuleAction: SimpleRuleAction<T>): Entry<T> {
        return this.put(atom, Rule.fromTerm(term, simpleRuleAction))
    }

    fun <T> getOrThrow(atom: Atom<T>): Entry<T> {
        return Objects.requireNonNull(
            terms[atom] as Entry<*>
        ) { "No rule called $atom" } as Entry<T>
    }

    fun <T> forward(atom: Atom<T>): Entry<T> {
        return this.getOrCreateEntry(atom)
    }

    private fun <T> getOrCreateEntry(atom: Atom<T>): Entry<T> {
        return terms.computeIfAbsent(atom) { Entry(it) } as Entry<T>
    }

    fun <T> named(atom: Atom<T>): Term {
        return Reference(this.getOrCreateEntry(atom), atom)
    }

    fun <T> namedWithAlias(atom: Atom<T>, atom2: Atom<T>): Term {
        return Reference(this.getOrCreateEntry(atom), atom2)
    }


    @JvmRecord
    internal data class Reference<T>(val ruleToParse: Entry<T>, val nameToStore: Atom<T>) : Term {
        override fun parse(parseState: ParseState, scope: Scope, control: Control): Boolean {
            val `object` = parseState.parse(this.ruleToParse)
            if (`object` == null) {
                return false
            } else {
                scope.put(this.nameToStore, `object`)
                return true
            }
        }
    }

    infix fun <T> Atom<T>.`=`(rule: Rule<T>): Entry<T> {
        return this@Dictionary.put(this,rule)
    }

    operator fun <T> get(atom: Atom<T>): Term {
        return named(atom)
    }

    operator fun <T> get(pair:Pair<Atom<T>, Atom<T>>): Term {
        return namedWithAlias(pair.first,pair.second)
    }
}