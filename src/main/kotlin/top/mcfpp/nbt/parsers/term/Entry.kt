package top.mcfpp.nbt.parsers.term

import top.mcfpp.nbt.parsers.rules.Rule
import java.util.*
import java.util.function.Supplier

class Entry<T>(private val name: Atom<T>) :  Supplier<String> {
    var value: Rule<T>? = null
    fun name(): Atom<T> {
        return this.name
    }

    fun value(): Rule<T> {
        return Objects.requireNonNull(this.value, this) as Rule<T>
    }

    override fun get(): String {
        return "Unbound rule " + this.name
    }
}
