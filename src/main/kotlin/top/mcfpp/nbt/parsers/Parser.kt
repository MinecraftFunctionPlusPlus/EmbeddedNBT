package top.mcfpp.nbt.parsers

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import top.mcfpp.nbt.NbtOps
import top.mcfpp.nbt.parsers.error.DelayedException
import top.mcfpp.nbt.parsers.error.ErrorCollector
import top.mcfpp.nbt.parsers.error.ErrorEntry
import top.mcfpp.nbt.parsers.state.CachedParseState
import top.mcfpp.nbt.parsers.term.Entry
import top.mcfpp.nbt.tags.CompoundTag
import top.mcfpp.nbt.tags.Tag
import top.mcfpp.utils.Component.Companion.translatable
import java.util.*
import java.util.function.Consumer
import java.util.stream.Collectors

object Parser {

    private val ERROR_TRAILING_DATA: SimpleCommandExceptionType =
        SimpleCommandExceptionType(translatable("argument.nbt.trailing"))

    private val ERROR_EXPECTED_COMPOUND: SimpleCommandExceptionType = SimpleCommandExceptionType(
        translatable("argument.nbt.expected.compound")
    )

    @JvmStatic
    @Throws(CommandSyntaxException::class)
    fun parse(string:String):Tag<*>{
        val stringReader = StringReader(string)
        return parse(stringReader)
    }

    @JvmStatic
    @Throws(CommandSyntaxException::class)
    fun parse(stringReader:StringReader): Tag<*> {
        val nameRule = SnbtGrammar.createParser(NbtOps.INSTANCE)

        val `object` = parseForCommands(stringReader,nameRule)
        stringReader.skipWhitespace()
        if (stringReader.canRead()) {
            throw ERROR_TRAILING_DATA.createWithContext(stringReader)
        } else {
            return `object`
        }
    }

    @JvmStatic
    @Throws(CommandSyntaxException::class)
    private fun castToCompoundOrThrow(stringReader: StringReader, tag: Tag<*>): CompoundTag {
        if (tag is CompoundTag) {
            return tag
        } else {
            throw ERROR_EXPECTED_COMPOUND.createWithContext(stringReader)
        }
    }


    @JvmStatic
    @Throws(CommandSyntaxException::class)
    fun parseCompoundFully(string: String?): CompoundTag {
        val stringReader = StringReader(string)
        return castToCompoundOrThrow(stringReader, parse(stringReader))
    }

    @JvmStatic
    @Throws(CommandSyntaxException::class)
    private fun <T : Any> parseForCommands(stringReader: StringReader, nameRule: Entry<T>): T {
        val longestOnly = ErrorCollector.LongestOnly()
        val stringReaderParserState = CachedParseState(longestOnly, stringReader)
        val optional: Optional<T> = (stringReaderParserState).parseTopRule(nameRule)
        if (optional.isPresent()) {
            return optional.get()
        } else {
            val list = longestOnly.entries()
            val list2 = list.stream()
                .mapMulti<Exception?> { errorEntry: ErrorEntry, consumer: Consumer<Exception?> ->
                    if (errorEntry.reason is DelayedException<*>) {
                        consumer.accept(errorEntry.reason.create(stringReader.string, errorEntry.cursor))
                    } else if (errorEntry.reason is Exception) {
                        consumer.accept(errorEntry.reason)
                    }
                }.toList()

            for (exception in list2) {
                if (exception is CommandSyntaxException) {
                    throw exception
                }
            }

            if (list2.size == 1 && list2.first() is RuntimeException) {
                throw list2.first()
            } else {
                throw IllegalStateException(
                    "Failed to parse: " + list.stream().map { obj: ErrorEntry -> obj.toString() }
                        .collect(Collectors.joining(", ")))
            }
        }
    }
}