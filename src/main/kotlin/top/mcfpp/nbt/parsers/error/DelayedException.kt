package top.mcfpp.nbt.parsers.error

import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import top.mcfpp.nbt.parsers.term.Term
import kotlin.Exception
import kotlin.Int
import kotlin.String

fun interface DelayedException<T : Exception> {
    fun create(string: String?, cursor: Int): T

    companion object {
        @JvmStatic
		fun create(simpleCommandExceptionType: SimpleCommandExceptionType): DelayedException<CommandSyntaxException> {
            return DelayedException<CommandSyntaxException> { string: String?, cursor: Int ->
                simpleCommandExceptionType.createWithContext(
                    Term.createReader(string, cursor)
                )
            }
        }

        @JvmStatic
		fun create(
            dynamicCommandExceptionType: DynamicCommandExceptionType,
            string: String?
        ): DelayedException<CommandSyntaxException> {
            return DelayedException<CommandSyntaxException> { string2: String?, cursor: Int ->
                dynamicCommandExceptionType.createWithContext(
                    Term.createReader(string2, cursor),
                    string
                )
            }
        }
    }
}
