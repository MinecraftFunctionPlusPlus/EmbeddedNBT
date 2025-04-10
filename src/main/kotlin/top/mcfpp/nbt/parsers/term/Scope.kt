package top.mcfpp.nbt.parsers.term

import com.google.common.annotations.VisibleForTesting
import top.mcfpp.utils.Math.growByHalf

@Suppress("UNCHECKED_CAST")
class Scope {
    private var stack = arrayOfNulls<Any>(128)
    private var topEntryKeyIndex = 0
    private var topMarkerKeyIndex = 0

    init {
        stack[0] = FRAME_START_MARKER
        stack[1] = null
    }

    private fun valueIndex(atom: Atom<*>): Int {
        var i = this.topEntryKeyIndex
        while (i > this.topMarkerKeyIndex) {
            val `object` = stack[i]

            assert(`object` is Atom<*>)

            if (`object` === atom) {
                return i + 1
            }
            i -= ENTRY_STRIDE
        }

        return NOT_FOUND
    }

    fun valueIndexForAny(vararg atoms: Atom<*>): Int {
        var i = this.topEntryKeyIndex
        while (i > this.topMarkerKeyIndex) {
            val `object` = stack[i]

            assert(`object` is Atom<*>)

            for (atom in atoms) {
                if (atom === `object`) {
                    return i + 1
                }
            }
            i -= ENTRY_STRIDE
        }

        return NOT_FOUND
    }

    private fun ensureCapacity(i: Int) {
        val j = stack.size
        val k = this.topEntryKeyIndex + 1
        val l = k + i * ENTRY_STRIDE
        if (l >= j) {
            val m = growByHalf(j, l + 1)
            val objects = arrayOfNulls<Any>(m)
            System.arraycopy(this.stack, 0, objects, 0, j)
            this.stack = objects
        }

        assert(this.validateStructure())
    }

    private fun setupNewFrame() {
        this.topEntryKeyIndex += ENTRY_STRIDE
        stack[topEntryKeyIndex] = FRAME_START_MARKER
        stack[topEntryKeyIndex + 1] = this.topMarkerKeyIndex
        this.topMarkerKeyIndex = this.topEntryKeyIndex
    }

    fun pushFrame() {
        this.ensureCapacity(1)
        this.setupNewFrame()

        assert(this.validateStructure())
    }

    private fun getPreviousMarkerIndex(i: Int): Int {
        return stack[i + 1] as Int
    }

    fun popFrame() {
        assert(this.topMarkerKeyIndex != 0)

        this.topEntryKeyIndex = this.topMarkerKeyIndex - ENTRY_STRIDE
        this.topMarkerKeyIndex = this.getPreviousMarkerIndex(this.topMarkerKeyIndex)

        assert(this.validateStructure())
    }

    fun splitFrame() {
        val i = this.topMarkerKeyIndex
        val j = (this.topEntryKeyIndex - this.topMarkerKeyIndex) / ENTRY_STRIDE
        this.ensureCapacity(j + 1)
        this.setupNewFrame()
        var k = i + ENTRY_STRIDE
        var l = this.topEntryKeyIndex

        for (m in 0 until j) {
            l += ENTRY_STRIDE
            val `object` = checkNotNull(stack[k])

            stack[l] = `object`
            stack[l + 1] = null
            k += ENTRY_STRIDE
        }

        this.topEntryKeyIndex = l

        assert(this.validateStructure())
    }

    fun clearFrameValues() {
        var i = this.topEntryKeyIndex
        while (i > this.topMarkerKeyIndex) {
            assert(stack[i] is Atom<*>)

            stack[i + 1] = null
            i -= ENTRY_STRIDE
        }

        assert(this.validateStructure())
    }

    fun mergeFrame() {
        val i = this.getPreviousMarkerIndex(this.topMarkerKeyIndex)
        var j = i
        var k = this.topMarkerKeyIndex

        while (k < this.topEntryKeyIndex) {
            j += ENTRY_STRIDE
            k += ENTRY_STRIDE
            val `object` = stack[k]

            assert(`object` is Atom<*>)

            val object2 = stack[k + 1]
            val object3 = stack[j]
            if (object3 !== `object`) {
                stack[j] = `object`
                stack[j + 1] = object2
            } else if (object2 != null) {
                stack[j + 1] = object2
            }
        }

        this.topEntryKeyIndex = j
        this.topMarkerKeyIndex = i

        assert(this.validateStructure())
    }

    fun <T> put(atom: Atom<T>, `object`: T?) {
        val i = this.valueIndex(atom)
        if (i != NOT_FOUND) {
            stack[i] = `object`
        } else {
            this.ensureCapacity(1)
            this.topEntryKeyIndex += ENTRY_STRIDE
            stack[topEntryKeyIndex] = atom
            stack[topEntryKeyIndex + 1] = `object`
        }

        assert(this.validateStructure())
    }

    operator fun <T> get(atom: Atom<T>): T? {
        val i = this.valueIndex(atom)
        return (if (i != NOT_FOUND) stack[i] else null) as T?
    }

    fun <T> getOrThrow(atom: Atom<T>): T {
        val i = this.valueIndex(atom)
        require(i != NOT_FOUND) { "No value for atom $atom" }
        return stack[i] as T
    }

    fun <T> getOrDefault(atom: Atom<T>, `object`: T): T {
        val i = this.valueIndex(atom)
        return (if (i != NOT_FOUND) stack[i] else `object`) as T
    }

    @SafeVarargs
    fun <T> getAny(vararg atoms: Atom<out T>): T? {
        val i = this.valueIndexForAny(*atoms)
        return (if (i != NOT_FOUND) stack[i] else null) as T?
    }

    @SafeVarargs
    fun <T> getAnyOrThrow(vararg atoms: Atom<out T>): T {
        val i = this.valueIndexForAny(*atoms)
        require(i != NOT_FOUND) { "No value for atoms " + atoms.contentToString() }
        return stack[i] as T
    }

    override fun toString(): String {
        val stringBuilder = StringBuilder()
        var bl = true

        var i = 0
        while (i <= this.topEntryKeyIndex) {
            val `object` = stack[i]
            val object2 = stack[i + 1]
            if (`object` === FRAME_START_MARKER) {
                stringBuilder.append('|')
                bl = true
            } else {
                if (!bl) {
                    stringBuilder.append(',')
                }

                bl = false
                stringBuilder.append(`object`).append(':').append(object2)
            }
            i += ENTRY_STRIDE
        }

        return stringBuilder.toString()
    }

    @VisibleForTesting
    fun lastFrame(): Map<Atom<*>?, *> {
        val hashMap: HashMap<Atom<*>?, Any?> = HashMap()

        var i = this.topEntryKeyIndex
        while (i > this.topMarkerKeyIndex) {
            val `object` = stack[i]
            val object2 = stack[i + 1]
            hashMap[`object` as Atom<*>?] = object2
            i -= ENTRY_STRIDE
        }

        return hashMap
    }

    fun hasOnlySingleFrame(): Boolean {
        for (i in this.topEntryKeyIndex downTo 1) {
            if (stack[i] === FRAME_START_MARKER) {
                return false
            }
        }

        check(stack[0] === FRAME_START_MARKER) { "Corrupted stack" }
        return true
    }

    private fun validateStructure(): Boolean {
        assert(this.topMarkerKeyIndex >= 0)

        assert(this.topEntryKeyIndex >= this.topMarkerKeyIndex)

        var i = 0
        while (i <= this.topEntryKeyIndex) {
            val `object` = stack[i]
            if (`object` !== FRAME_START_MARKER && `object` !is Atom<*>) {
                return false
            }
            i += ENTRY_STRIDE
        }

        var ix = this.topMarkerKeyIndex
        while (ix != 0) {
            val `object` = stack[ix]
            if (`object` !== FRAME_START_MARKER) {
                return false
            }
            ix = this.getPreviousMarkerIndex(ix)
        }

        return true
    }

    companion object {
        private const val NOT_FOUND = -1
        private val FRAME_START_MARKER: Any = object : Any() {
            override fun toString(): String {
                return "frame"
            }
        }
        private const val ENTRY_STRIDE = 2
    }
}
