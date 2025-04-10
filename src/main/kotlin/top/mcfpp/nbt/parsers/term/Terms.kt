package top.mcfpp.nbt.parsers.term

class Terms {


    companion object {
        operator fun String.not(): Term {
            return Term.word(this)
        }

        operator fun Char.not(): Term {
            return Term.character(this)
        }

        operator fun CharGroup.not(): Term {
            return Term.characters(this.chars[0], this.chars[1])
        }

        data class CharGroup(val chars: MutableList<Char>)

        operator fun Char.div(char:Char): CharGroup {
            return CharGroup(arrayListOf(this,char))
        }


        data class NameTimeGroup<T>(val entry: Entry<T>, val time:Int)

        operator fun <T> Entry<T>.times(time:Int): NameTimeGroup<T> {
            return NameTimeGroup(this,time)
        }

        operator fun <T> Atom<List<T>>.rem(other: NameTimeGroup<T>): Term {
            return Term.repeated(other.entry,this,other.time)
        }

        data class WithoutTerm(val term: Term)

        operator fun Term.not(): WithoutTerm {
            return WithoutTerm(this)
        }

        data class NameTimeSeparatorGroup<T>(val entry: Entry<T>, val time:Int, val separator: Term, val without:Boolean)

        operator fun <T> NameTimeGroup<T>.rangeTo(other: Term): NameTimeSeparatorGroup<T> {
            return NameTimeSeparatorGroup(this.entry,this.time,other,true)
        }
        operator fun <T> NameTimeGroup<T>.rangeTo(other: WithoutTerm): NameTimeSeparatorGroup<T> {
            return NameTimeSeparatorGroup(this.entry,this.time,other.term,false)
        }

        operator fun <T> Atom<List<T>>.rem(other: NameTimeSeparatorGroup<T>): Term {
            return if (other.without){
                Term.Companion.repeatedWithTrailingSeparator(other.entry,this,other.separator,other.time)
            } else{
                Term.Companion.repeatedWithoutTrailingSeparator(other.entry,this,other.separator,other.time)
            }
        }

        operator fun Term.unaryPlus(): Term {
            return Term.positiveLookahead(this)
        }

        operator fun Term.unaryMinus(): Term {
            return Term.negativeLookahead(this)
        }

        operator fun <T> Atom<T>.rem(other:T): Term {
            return Term.marker(this,other)
        }

        operator fun Term.rangeTo(other: Term): Term {
            return mergeSequence(this, other)
        }
        private fun mergeSequence(term: Term, other: Term): Sequence {
            val terms = mutableListOf<Term>().apply {
                if (term is Sequence) addAll(term.elements) else add(term)
                if (other is Sequence) addAll(other.elements) else add(other)
            }
            return Sequence(terms.toTypedArray<Term>())
        }

        operator fun Term.div(other: Term): Term {
            return mergeAlternative(this,other)
        }
        private fun mergeAlternative(term: Term, other: Term): Alternative {
            val terms = mutableListOf<Term>().apply {
                if (term is Alternative) addAll(term.elements) else add(term)
                if (other is Alternative) addAll(other.elements) else add(other)
            }
            return Alternative(terms.toTypedArray<Term>())
        }

        fun Term.opt(): Term {
            return Term.optional(this)
        }
    }
}