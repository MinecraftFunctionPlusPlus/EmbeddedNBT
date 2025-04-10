package top.mcfpp.nbt.parsers.error

import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import top.mcfpp.nbt.parsers.error.DelayedException.Companion.create
import top.mcfpp.utils.Component.Companion.translatable
import top.mcfpp.utils.Component.Companion.translatableEscape

object SnbtException {
    val ERROR_NUMBER_PARSE_FAILURE: DynamicCommandExceptionType = DynamicCommandExceptionType {
        translatableEscape("snbt.parser.number_parse_failure", it)
    }
    @JvmField
    val ERROR_EXPECTED_HEX_ESCAPE: DynamicCommandExceptionType = DynamicCommandExceptionType {
        translatableEscape("snbt.parser.expected_hex_escape", it)
    }
    @JvmField
    val ERROR_INVALID_CODEPOINT: DynamicCommandExceptionType = DynamicCommandExceptionType {
        translatableEscape("snbt.parser.invalid_codepoint", it)
    }
    @JvmField
    val ERROR_NO_SUCH_OPERATION: DynamicCommandExceptionType = DynamicCommandExceptionType {
        translatableEscape("snbt.parser.no_such_operation", it)
    }
    @JvmField
    val ERROR_EXPECTED_INTEGER_TYPE: DelayedException<CommandSyntaxException> = create(
        SimpleCommandExceptionType(translatable("snbt.parser.expected_integer_type"))
    )
    @JvmField
    val ERROR_EXPECTED_FLOAT_TYPE: DelayedException<CommandSyntaxException> = create(
        SimpleCommandExceptionType(translatable("snbt.parser.expected_float_type"))
    )
    @JvmField
    val ERROR_EXPECTED_NON_NEGATIVE_NUMBER: DelayedException<CommandSyntaxException> = create(
        SimpleCommandExceptionType(translatable("snbt.parser.expected_non_negative_number"))
    )
    @JvmField
    val ERROR_INVALID_CHARACTER_NAME: DelayedException<CommandSyntaxException> = create(
        SimpleCommandExceptionType(translatable("snbt.parser.invalid_character_name"))
    )
    @JvmField
    val ERROR_INVALID_ARRAY_ELEMENT_TYPE: DelayedException<CommandSyntaxException> = create(
        SimpleCommandExceptionType(translatable("snbt.parser.invalid_array_element_type"))
    )
    @JvmField
    val ERROR_INVALID_UNQUOTED_START: DelayedException<CommandSyntaxException> = create(
        SimpleCommandExceptionType(translatable("snbt.parser.invalid_unquoted_start"))
    )
    @JvmField
    val ERROR_EXPECTED_UNQUOTED_STRING: DelayedException<CommandSyntaxException> = create(
        SimpleCommandExceptionType(translatable("snbt.parser.expected_unquoted_string"))
    )
    @JvmField
    val ERROR_INVALID_STRING_CONTENTS: DelayedException<CommandSyntaxException> = create(
        SimpleCommandExceptionType(translatable("snbt.parser.invalid_string_contents"))
    )
    @JvmField
    val ERROR_EXPECTED_BINARY_NUMERAL: DelayedException<CommandSyntaxException> = create(
        SimpleCommandExceptionType(translatable("snbt.parser.expected_binary_numeral"))
    )
    @JvmField
    val ERROR_UNDESCORE_NOT_ALLOWED: DelayedException<CommandSyntaxException> = create(
        SimpleCommandExceptionType(translatable("snbt.parser.undescore_not_allowed"))
    )
    @JvmField
    val ERROR_EXPECTED_DECIMAL_NUMERAL: DelayedException<CommandSyntaxException> = create(
        SimpleCommandExceptionType(translatable("snbt.parser.expected_decimal_numeral"))
    )
    @JvmField
    val ERROR_EXPECTED_HEX_NUMERAL: DelayedException<CommandSyntaxException> = create(
        SimpleCommandExceptionType(translatable("snbt.parser.expected_hex_numeral"))
    )
    @JvmField
    val ERROR_EMPTY_KEY: DelayedException<CommandSyntaxException> = create(
        SimpleCommandExceptionType(translatable("snbt.parser.empty_key"))
    )
    @JvmField
    val ERROR_LEADING_ZERO_NOT_ALLOWED: DelayedException<CommandSyntaxException> = create(
        SimpleCommandExceptionType(translatable("snbt.parser.leading_zero_not_allowed"))
    )
    @JvmField
    val ERROR_INFINITY_NOT_ALLOWED: DelayedException<CommandSyntaxException> = create(
        SimpleCommandExceptionType(translatable("snbt.parser.infinity_not_allowed"))
    )

    fun createNumberParseError(numberFormatException: NumberFormatException): DelayedException<CommandSyntaxException> {
        return create(ERROR_NUMBER_PARSE_FAILURE, numberFormatException.message)
    }

    @JvmField
    val ERROR_EXPECTED_STRING_UUID: DelayedException<CommandSyntaxException> = create(
        SimpleCommandExceptionType(translatable("snbt.parser.expected_string_uuid"))
    )
    @JvmField
    val ERROR_EXPECTED_NUMBER_OR_BOOLEAN: DelayedException<CommandSyntaxException> = create(
        SimpleCommandExceptionType(translatable("snbt.parser.expected_number_or_boolean"))
    )
}
