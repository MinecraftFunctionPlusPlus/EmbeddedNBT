package top.mcfpp.nbt.parsers.error


@JvmRecord
data class ErrorEntry(val cursor: Int, val suggestions: SuggestionSupplier, val reason: Any?)


interface ErrorCollector {
    fun store(i: Int, suggestionSupplier: SuggestionSupplier, `object`: Any?)

    fun store(i: Int, `object`: Any?) {
        this.store(i, SuggestionSupplier.empty(), `object`)
    }

    fun finish(i: Int)

    class LongestOnly : ErrorCollector {
        private val entries: MutableList<MutableErrorEntry> = ArrayList(16)
        private var nextErrorEntry = 0
        private var lastCursor = -1

        private fun discardErrorsFromShorterParse(i: Int) {
            if (i > this.lastCursor) {
                this.lastCursor = i
                this.nextErrorEntry = 0
            }
        }

        override fun finish(i: Int) {
            this.discardErrorsFromShorterParse(i)
        }

        override fun store(i: Int, suggestionSupplier: SuggestionSupplier, `object`: Any?) {
            this.discardErrorsFromShorterParse(i)
            if (i == this.lastCursor) {
                this.addErrorEntry(suggestionSupplier, `object`)
            }
        }

        private fun addErrorEntry(suggestionSupplier: SuggestionSupplier, `object`: Any?) {
            val j = nextErrorEntry++
            val mutableErrorEntry = MutableErrorEntry()
            mutableErrorEntry.suggestions = suggestionSupplier
            mutableErrorEntry.reason = `object`
            if (j >= entries.size) {
                entries.add(j, mutableErrorEntry)
            } else {
                entries[j] = mutableErrorEntry
            }
        }

        fun entries(): List<ErrorEntry> {
            val i = this.nextErrorEntry
            if (i == 0) {
                return listOf()
            } else {
                val list: MutableList<ErrorEntry> = ArrayList(i)

                for (j in 0 until i) {
                    val mutableErrorEntry = entries[j]
                    list.add(ErrorEntry(this.lastCursor, mutableErrorEntry.suggestions, mutableErrorEntry.reason))
                }

                return list
            }
        }

        fun cursor(): Int {
            return this.lastCursor
        }

        internal class MutableErrorEntry {
            var suggestions: SuggestionSupplier = SuggestionSupplier.empty()
            var reason: Any? = "empty"
        }
    }

    class Nop : ErrorCollector {
        override fun store(i: Int, suggestionSupplier: SuggestionSupplier, `object`: Any?) {
        }

        override fun finish(i: Int) {
        }
    }
}


