package top.mcfpp.nbt.parsers.term

@JvmRecord
data class Atom<T>(val name: String) {
    override fun toString(): String {
        return "<" + this.name + ">"
    }

    companion object {
        fun <T> of(string: String): Atom<T> {
            return Atom(string)
        }
    }
}
