package top.mcfpp.nbt.parsers.state

import com.mojang.brigadier.StringReader
import top.mcfpp.nbt.parsers.error.ErrorCollector
import top.mcfpp.nbt.parsers.term.Control
import top.mcfpp.nbt.parsers.term.Entry
import top.mcfpp.nbt.parsers.term.Scope
import java.util.*

interface ParseState {
    fun scope(): Scope

    fun errorCollector(): ErrorCollector

    fun <T:Any> parseTopRule(entry: Entry<T>): Optional<T> {
        val `object` = this.parse(entry)
        if (`object` != null) {
            errorCollector().finish(this.mark())
        }

        check(scope().hasOnlySingleFrame()) { "Malformed scope: " + this.scope() }
        return Optional.ofNullable(`object`)
    }

    fun <T> parse(entry: Entry<T>): T?

    /**
     * @return 需要被处理的输入
     */
    fun input(): StringReader

    /**
     * @return 当前指针位置
     */
    fun mark(): Int

    /**
     * @param i 重设当前指针位置
     */
    fun restore(i: Int)

    fun acquireControl(): Control

    fun releaseControl()

    fun silent(): ParseState
}
