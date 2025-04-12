package top.mcfpp.nbt.parsers.state

import com.mojang.brigadier.StringReader
import top.mcfpp.nbt.parsers.error.ErrorCollector
import top.mcfpp.nbt.parsers.term.*

class CachedParseState(private val errorCollector: ErrorCollector,private val input: StringReader) : ParseState {
    private val positionCaches: MutableList<PositionCache> = ArrayList(256)
    private val scope = Scope()
    private val controlCache: MutableList<SimpleControl> = ArrayList(16)
    private var nextControlToReturn = 0
    private val silent: Silent = Silent()

    override fun input(): StringReader {
        return this.input
    }

    override fun mark(): Int {
        return input.cursor
    }

    override fun restore(i: Int) {
        input.cursor = i
    }

    override fun scope(): Scope {
        return this.scope
    }

    override fun errorCollector(): ErrorCollector {
        return this.errorCollector
    }

    override fun <T> parse(entry: Entry<T>): T? {
        val i = this.mark()
        if (i >= positionCaches.size) {
            for (index in positionCaches.size..i) {
                positionCaches.add(PositionCache())
            }
        }
        val positionCache = positionCaches[i]
        val atom: Atom<*> = entry.name()
        if (positionCache.contains(atom)) {
            val cacheEntry = positionCache.getValue<T>(atom)
            if (cacheEntry != null) {
                if (cacheEntry === CacheEntry.NEGATIVE) {
                    return null
                }

                this.restore(cacheEntry.markAfterParse)
                return cacheEntry.value
            }
        }

        val `object` = entry.value().parse(this)
        val cacheEntry2: CacheEntry<T>
        if (`object` == null) {
            cacheEntry2 = CacheEntry.negativeEntry()
        } else {
            val k = this.mark()
            cacheEntry2 = CacheEntry(`object`, k)
        }

        positionCache.setValue(atom, cacheEntry2)
        return `object`
    }

    override fun acquireControl(): Control {
        val j = nextControlToReturn++
        if (j >= controlCache.size) {
            controlCache.add(SimpleControl())
        }
        val simpleControl = controlCache[j]
        simpleControl.reset()
        return simpleControl
    }

    override fun releaseControl() {
        nextControlToReturn--
    }

    override fun silent(): ParseState {
        return this.silent
    }

    @JvmRecord
    internal data class CacheEntry<T>(val value: T?, val markAfterParse: Int) {
        companion object {
            val NEGATIVE: CacheEntry<*> = CacheEntry<Any?>(null, -1)

            @Suppress("UNCHECKED_CAST")
            fun <T> negativeEntry(): CacheEntry<T> {
                return NEGATIVE as CacheEntry<T>
            }
        }
    }

    internal class PositionCache {
        private val cacheMap: MutableMap<Atom<*>, CacheEntry<*>> = HashMap.newHashMap(8)

        fun contains(atom: Atom<*>): Boolean {
            return cacheMap.containsKey(atom)
        }

        @Suppress("UNCHECKED_CAST")
        fun <T> getValue(atom: Atom<*>): CacheEntry<T>? {
            return cacheMap[atom] as CacheEntry<T>?
        }

        fun setValue(atom: Atom<*>, cacheEntry: CacheEntry<*>) {
            cacheMap[atom] = cacheEntry
        }

        companion object {
            private const val NOT_FOUND = -1
        }
    }

    //执行相同的解析功能， 但不记录错误
    internal inner class Silent : ParseState {
        private val silentCollector: ErrorCollector = ErrorCollector.Nop()

        override fun errorCollector(): ErrorCollector {
            return this.silentCollector
        }

        override fun scope(): Scope {
            return this@CachedParseState.scope()
        }

        override fun <T> parse(entry: Entry<T>): T? {
            return this@CachedParseState.parse(entry)
        }

        override fun input(): StringReader {
            return this@CachedParseState.input()
        }

        override fun mark(): Int {
            return this@CachedParseState.mark()
        }

        override fun restore(i: Int) {
            this@CachedParseState.restore(i)
        }

        override fun acquireControl(): Control {
            return this@CachedParseState.acquireControl()
        }

        override fun releaseControl() {
            this@CachedParseState.releaseControl()
        }

        override fun silent(): ParseState {
            return this
        }
    }


}
