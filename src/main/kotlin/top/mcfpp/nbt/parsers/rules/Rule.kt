package top.mcfpp.nbt.parsers.rules

import top.mcfpp.nbt.parsers.term.Scope
import top.mcfpp.nbt.parsers.term.Term
import top.mcfpp.nbt.parsers.state.ParseState
import top.mcfpp.nbt.parsers.term.Control

interface Rule<T> {
    fun parse(parseState: ParseState): T?

    fun interface RuleAction<T> {
        fun run(parseState: ParseState): T?
    }

    fun interface SimpleRuleAction<T> : RuleAction<T> {
        fun run(scope: Scope): T?

        override fun run(parseState: ParseState): T? {
            return this.run(parseState.scope())
        }
    }

    @JvmRecord
    data class WrappedTerm<T:Any>(val action: RuleAction<T>, val child: Term) : Rule<T> {
        override fun parse(parseState: ParseState): T? {
            val scope = parseState.scope()
            scope.pushFrame()

            val var3: Any?
            try {
                if (!child.parse(parseState, scope, Control.UNBOUND)) {
                    return null
                }

                var3 = action.run(parseState)
            } finally {
                scope.popFrame()
            }

            return var3 as T?
        }
    }

    companion object {
        fun <T:Any> fromTerm(term: Term, ruleAction: RuleAction<T>): Rule<T> {
            return WrappedTerm(ruleAction, term)
        }

        fun <T:Any> fromTerm(term: Term, simpleRuleAction: SimpleRuleAction<T>): Rule<T> {
            return WrappedTerm(simpleRuleAction, term)
        }

        fun <T : Any> Term.state(ruleAction: RuleAction<T>): Rule<T> {
            return fromTerm(this, ruleAction)
        }

        fun <T : Any> Term.scope(ruleAction: SimpleRuleAction<T>): Rule<T> {
            return fromTerm(this, ruleAction)
        }



    }
}
