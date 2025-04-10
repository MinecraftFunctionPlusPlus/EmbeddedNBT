package top.mcfpp.nbt.parsers


import com.google.common.collect.ImmutableMap
import com.mojang.serialization.DynamicOps
import top.mcfpp.nbt.parsers.error.DelayedException
import top.mcfpp.nbt.parsers.rules.GreedyPatternParseRule
import top.mcfpp.nbt.parsers.rules.UnquotedStringParseRule
import top.mcfpp.nbt.parsers.rules.SimpleHexLiteralParseRule
import top.mcfpp.nbt.parsers.state.ParseState
import top.mcfpp.nbt.parsers.rules.Rule.Companion.scope
import top.mcfpp.nbt.parsers.rules.Rule.Companion.state
import top.mcfpp.nbt.parsers.term.Term.Companion.cut
import top.mcfpp.nbt.parsers.term.Term.Companion.fail
import top.mcfpp.nbt.parsers.term.Terms.Companion.div
import top.mcfpp.nbt.parsers.term.Terms.Companion.not
import top.mcfpp.nbt.parsers.term.Terms.Companion.opt
import top.mcfpp.nbt.parsers.term.Terms.Companion.rangeTo
import top.mcfpp.nbt.parsers.term.Terms.Companion.rem
import top.mcfpp.nbt.parsers.term.Terms.Companion.times
import top.mcfpp.nbt.parsers.term.Terms.Companion.unaryPlus
import top.mcfpp.nbt.parsers.error.SnbtException
import top.mcfpp.nbt.parsers.term.Atom
import top.mcfpp.nbt.parsers.term.Dictionary
import top.mcfpp.nbt.parsers.term.Entry
import top.mcfpp.nbt.parsers.term.Scope

import java.util.*

object SnbtGrammar {

    fun <T : Any> createParser(dynamicOps: DynamicOps<T>): Entry<T> {
        Dictionary().apply {
            val trueObj = dynamicOps.createBoolean(true)
            val falseOpj = dynamicOps.createBoolean(false)
            val emptyMap = dynamicOps.emptyMap()
            val emptyList = dynamicOps.emptyList()

            val signAtom: Atom<Sign> = Atom.of("sign")
            signAtom `=` (
                    (!'+'.. signAtom%Sign.PLUS)
                    / (!'-'.. signAtom%Sign.MINUS)
            ).scope{ it.getOrThrow(signAtom) }

            val intSufAtom: Atom<IntegerSuffix> = Atom.of("integer_suffix")
            intSufAtom `=` (
                (!('u'/'U')
                        ..(!('b'/'B')..intSufAtom%IntegerSuffix(SignedPrefix.UNSIGNED, TypeSuffix.BYTE))
                        /(!('s'/ 'S')..intSufAtom%IntegerSuffix(SignedPrefix.UNSIGNED, TypeSuffix.SHORT))
                        /(!('i'/ 'I')..(intSufAtom%IntegerSuffix(SignedPrefix.UNSIGNED, TypeSuffix.INT)))
                        /(!('l'/ 'L')..(intSufAtom%IntegerSuffix(SignedPrefix.UNSIGNED, TypeSuffix.LONG))))
                /(!('s'/'S')
                        ..(!('b'/'B')..intSufAtom%IntegerSuffix(SignedPrefix.SIGNED, TypeSuffix.BYTE))
                        /(!('s'/ 'S')..intSufAtom%IntegerSuffix(SignedPrefix.SIGNED, TypeSuffix.SHORT))
                        /(!('i'/ 'I')..(intSufAtom%IntegerSuffix(SignedPrefix.SIGNED, TypeSuffix.INT)))
                        /(!('l'/ 'L')..(intSufAtom%IntegerSuffix(SignedPrefix.SIGNED, TypeSuffix.LONG))))
                /(!('b'/'B')..intSufAtom%IntegerSuffix(null, TypeSuffix.BYTE))
                /(!('s'/ 'S')..intSufAtom%IntegerSuffix(null, TypeSuffix.SHORT))
                /(!('i'/ 'I')..intSufAtom%IntegerSuffix(null, TypeSuffix.INT))
                /(!('l'/ 'L')..intSufAtom%IntegerSuffix(null, TypeSuffix.LONG))
            ).scope{ it.getOrThrow(intSufAtom) }

            val biNumAtom = Atom.of<String>("binary_numeral")
            biNumAtom `=` SnbtGrammarUtils.BINARY_NUMERAL

            val decNumAtom = Atom.of<String>("decimal_numeral")
            decNumAtom `=` SnbtGrammarUtils.DECIMAL_NUMERAL

            val hexNumAtom = Atom.of<String>("hex_numeral")
            hexNumAtom`=` SnbtGrammarUtils.HEX_NUMERAL

            val intLiteralAtom: Atom<IntegerLiteral> = Atom.of("integer_literal")
            val intNumRule = intLiteralAtom `=`
                (
                    this[signAtom].opt()
                    ..(!'0'..cut()
                        ..(!('x'/ 'X').. cut().. this[hexNumAtom])
                        /(!('b'/ 'B').. this[biNumAtom])
                        /(this[decNumAtom]..cut()..fail(SnbtException.ERROR_LEADING_ZERO_NOT_ALLOWED))
                        /(decNumAtom%"0")
                    ) /this[decNumAtom]
                    ..this[intSufAtom].opt()
                ).scope { scope: Scope ->
                    val integerSuffix = scope.getOrDefault(intSufAtom, IntegerSuffix.EMPTY)
                    val sign = scope.getOrDefault(signAtom, Sign.PLUS)
                    val string = scope[decNumAtom]
                    if (string != null) {
                        return@scope IntegerLiteral(sign, Base.DECIMAL, string, integerSuffix)
                    } else {
                        val string2 = scope[hexNumAtom]
                        if (string2 != null) {
                            return@scope IntegerLiteral(sign, Base.HEX, string2, integerSuffix)
                        } else {
                            val string3 = scope.getOrThrow(biNumAtom)
                            return@scope IntegerLiteral(sign, Base.BINARY, string3, integerSuffix)
                        }
                    }
                }

            val floatTypeSufAtom: Atom<TypeSuffix> = Atom.of("float_type_suffix")
            floatTypeSufAtom `=` (
                (!('f'/ 'F').. floatTypeSufAtom%TypeSuffix.FLOAT)
                /(!('d'/ 'D').. floatTypeSufAtom%TypeSuffix.DOUBLE)
            ).scope { it.getOrThrow(floatTypeSufAtom) }

            val floatExpPartAtom: Atom<Signed<String>> = Atom.of("float_exponent_part")
            floatExpPartAtom `=` (
                !('e'/ 'E')..this[signAtom].opt()..this[decNumAtom]
            ).scope { scope: Scope ->
                Signed(
                    scope.getOrDefault(signAtom, Sign.PLUS),
                    scope.getOrThrow(decNumAtom)
                )
            }

            val floatWholePartAtom = Atom.of<String>("float_whole_part")
            val floatFractionPartAtom = Atom.of<String>("float_fraction_part")
            val floatLiteralAtom = Atom.of<T>("float_literal")
            floatLiteralAtom `=` (
                this[signAtom].opt()..
                (
                    (
                        this[decNumAtom to floatWholePartAtom]..
                        !'.'..
                        cut()..
                        this[decNumAtom to floatFractionPartAtom].opt()..
                        this[floatExpPartAtom].opt()..
                        this[floatTypeSufAtom].opt()
                    )

                    /(
                        !'.'..
                        cut()..
                        this[decNumAtom to floatFractionPartAtom]..
                        this[floatExpPartAtom].opt()..
                        this[floatTypeSufAtom].opt()
                    )
                    /(
                        this[decNumAtom to floatWholePartAtom]..
                        this[floatExpPartAtom]..
                        cut()..
                        this[floatTypeSufAtom].opt()
                    )
                    /(
                        this[decNumAtom to floatWholePartAtom]..
                        this[floatExpPartAtom].opt()..
                        this[floatTypeSufAtom]
                    )
                )
            ).state { parseState: ParseState ->
                val scope = parseState.scope()
                val sign = scope.getOrDefault(signAtom, Sign.PLUS)
                val string = scope[floatWholePartAtom]
                val string2 = scope[floatFractionPartAtom]
                val signed = scope[floatExpPartAtom]
                val typeSuffix = scope[floatTypeSufAtom]
                SnbtGrammarUtils.createFloat(dynamicOps, sign, string, string2, signed, typeSuffix, parseState)
            }

            val strHex2Atom = Atom.of<String>("string_hex_2")
            strHex2Atom `=` SimpleHexLiteralParseRule(2)

            val strHex4Atom = Atom.of<String>("string_hex_4")
            strHex4Atom `=` SimpleHexLiteralParseRule(4)

            val strHex8Atom = Atom.of<String>("string_hex_8")
            strHex8Atom `=` SimpleHexLiteralParseRule(8)

            val strUnicodeAtom = Atom.of<String>("string_unicode_name")
            strUnicodeAtom `=` GreedyPatternParseRule(SnbtGrammarUtils.UNICODE_NAME, SnbtException.ERROR_INVALID_CHARACTER_NAME)

            val strEscSeqAtom = Atom.of<String>("string_escape_sequence")
            strEscSeqAtom `=` (
                (!'b'.. strEscSeqAtom%"\b")
                /(!'s'.. strEscSeqAtom%" ")
                /(!'t'.. strEscSeqAtom%"\t")
                /(!'n'.. strEscSeqAtom%"\n")
                /(!'f'.. strEscSeqAtom%"\u000c")
                /(!'r'.. strEscSeqAtom%"\r")
                /(!'\\'.. strEscSeqAtom%"\\")
                /(!'\''.. strEscSeqAtom%"'")
                /(!'"'.. strEscSeqAtom%"\"")
                /(!'x'.. this[strHex2Atom])
                /(!'u'.. this[strHex4Atom])
                /(!'U'.. this[strHex8Atom])
                /(!'N'..!'{'..this[strUnicodeAtom]..!'}')
            ).state { parseState: ParseState ->
                val scope = parseState.scope()
                val string = scope.getAny(strEscSeqAtom)
                if (string != null) {
                    return@state string
                } else {
                    val string2 = scope.getAny(strHex2Atom, strHex4Atom, strHex8Atom)
                    if (string2 != null) {
                        val i = HexFormat.fromHexDigits(string2)
                        if (!Character.isValidCodePoint(i)) {
                            parseState.errorCollector().store(
                                parseState.mark(),
                                DelayedException.create(
                                    SnbtException.ERROR_INVALID_CODEPOINT,
                                    String.format(Locale.ROOT, "U+%08X", i)
                                )
                            )
                            return@state null
                        } else {
                            return@state Character.toString(i)
                        }
                    } else {
                        val string3 = scope.getOrThrow(strUnicodeAtom)

                        val j: Int
                        try {
                            j = Character.codePointOf(string3)
                        } catch (var12x: IllegalArgumentException) {
                            parseState.errorCollector().store(parseState.mark(), SnbtException.ERROR_INVALID_CHARACTER_NAME)
                            return@state null
                        }

                        return@state Character.toString(j)
                    }
                }
            }
            val strPlainContAtom = Atom.of<String>("string_plain_contents")
            strPlainContAtom`=` SnbtGrammarUtils.PLAIN_STRING_CHUNK

            val strChunkAtom = Atom.of<List<String>>("string_chunks")
            val strContAtom = Atom.of<String>("string_contents")
            val singleQuotedStrChunkAtom = Atom.of<String>("single_quoted_string_chunk")
            val singleQuotedStrChunkRule = singleQuotedStrChunkAtom `=` (
                this[strPlainContAtom to strContAtom]
                /(!'\\'.. this[strEscSeqAtom to strContAtom])
                /(!'"'.. strContAtom%"\"")
            ).scope { it.getOrThrow(strContAtom) }

            val singleQuotedStrContAtom = Atom.of<String>("single_quoted_string_contents")
            singleQuotedStrContAtom `=` (
                strChunkAtom%(singleQuotedStrChunkRule*0)
            ).scope { SnbtGrammarUtils.joinList(it.getOrThrow(strChunkAtom)) }

            val doubleQuotedStrChunkAtom = Atom.of<String>("double_quoted_string_chunk")
            val doubleQuotedStrChunkRule = doubleQuotedStrChunkAtom `=` (
                this[strPlainContAtom to strContAtom]
                /(!'\\'.. this[strEscSeqAtom to strContAtom])
                /(!'\''.. strContAtom%"'")
            ).scope { it.getOrThrow(strContAtom) }

            val doubleQuotedStrContAtom = Atom.of<String>("double_quoted_string_contents")
            doubleQuotedStrContAtom`=`(
                strChunkAtom%(doubleQuotedStrChunkRule*0)
            ).scope() { SnbtGrammarUtils.joinList(it.getOrThrow(strChunkAtom)) }

            val quotedStrLiteralAtom = Atom.of<String>("quoted_string_literal")
            quotedStrLiteralAtom`=`(
                (!'"'..cut()..this[doubleQuotedStrContAtom to strContAtom].opt()..!'"')
                /(!'\''..this[singleQuotedStrContAtom to strContAtom].opt()..!'\'')
            ).scope { it.getOrThrow(strContAtom) }

            val unquotedStrAtom = Atom.of<String>("unquoted_string")
            unquotedStrAtom `=` UnquotedStringParseRule(1, SnbtException.ERROR_EXPECTED_UNQUOTED_STRING)

            val literalAtom = Atom.of<T>("literal")
            val argsAtom = Atom.of<List<T>>("arguments")
            argsAtom`=`(
                argsAtom%(forward(literalAtom)*0..!',')
            ).scope { it.getOrThrow(argsAtom) }

            val unquotedStrOrBuiltinAtom = Atom.of<T>("unquoted_string_or_builtin")
            unquotedStrOrBuiltinAtom`=`(
                this[unquotedStrAtom]..(!'('..this[argsAtom]..!')').opt()
            ).state { parseState: ParseState ->
                val scope = parseState.scope()
                val string = scope.getOrThrow(unquotedStrAtom)
                if (string.isNotEmpty() && SnbtGrammarUtils.isAllowedToStartUnquotedString(string[0])) {
                    val list = scope.get(argsAtom)
                    if (list != null) {
                        val builtinKey = SnbtOperationsKt.BuiltinKey(string, list.size)
                        val builtinOperation = SnbtOperationsKt.BUILTIN_OPERATIONS[builtinKey]
                        if (builtinOperation != null) {
                            return@state builtinOperation.run<T>(dynamicOps, list, parseState)
                        } else {
                            parseState.errorCollector().store(
                                parseState.mark(),
                                DelayedException.create(SnbtException.ERROR_NO_SUCH_OPERATION, builtinKey.toString())
                            )
                            return@state null
                        }
                    } else if (string.equals("true", ignoreCase = true)) {
                        return@state trueObj
                    } else {
                        return@state if (string.equals(
                                "false",
                                ignoreCase = true
                            )
                        ) falseOpj else dynamicOps.createString(string)
                    }
                } else {
                    parseState.errorCollector()
                        .store(parseState.mark(), SnbtOperationsKt.BUILTIN_IDS, SnbtException.ERROR_INVALID_UNQUOTED_START)
                    return@state null
                }
            }
            val mapKeyAtom = Atom.of<String>("map_key")
            mapKeyAtom `=` (
                this[quotedStrLiteralAtom]/ this[unquotedStrAtom]
            ).scope { it.getAnyOrThrow(quotedStrLiteralAtom, unquotedStrAtom) }

            val mapEntryAtom = Atom.of<Map.Entry<String, T>>("map_entry")
            val mapEntryRule = mapEntryAtom `=` (
                (this[mapKeyAtom].. !':'.. this[literalAtom])
            ).state { parseState: ParseState ->
                val scope = parseState.scope()
                val string = scope.getOrThrow(mapKeyAtom)
                if (string.isEmpty()) {
                    parseState.errorCollector().store(parseState.mark(), SnbtException.ERROR_EMPTY_KEY)
                    return@state null
                } else {
                    val objectx = scope.getOrThrow(literalAtom)
                    return@state java.util.Map.entry<String, T>(string, objectx)
                }
            }

            val mapEntriesAtom = Atom.of<List<Map.Entry<String, T>>>("map_entries")
            mapEntriesAtom`=`(
                mapEntriesAtom%(mapEntryRule*0..!',')
            ).scope { it.getOrThrow(mapEntriesAtom) }

            val mapLiteralAtom = Atom.of<T>("map_literal")
            mapLiteralAtom `=` (
                !'{'.. this[mapEntriesAtom].. !'}'
            ).scope { scope: Scope ->
                val list =
                    scope.getOrThrow(mapEntriesAtom)
                if (list.isEmpty()) {
                    return@scope emptyMap
                } else {
                    val builder =
                        ImmutableMap.builderWithExpectedSize<T, T>(list.size)

                    for ((key, value) in list) {
                        builder.put(dynamicOps.createString(key), value)
                    }

                    return@scope dynamicOps.createMap(builder.buildKeepingLast())
                }
            }

            val listEntriesAtom = Atom.of<List<T>>("list_entries")
            listEntriesAtom `=` (
                listEntriesAtom%(forward(literalAtom)*0.. !',')
            ).scope { it.getOrThrow(listEntriesAtom) }

            val arrayPreAtom: Atom<ArrayPrefix> = Atom.of("array_prefix")
            arrayPreAtom`=`(
                (!'B'.. arrayPreAtom%ArrayPrefix.BYTE)
                /(!'L'.. arrayPreAtom%ArrayPrefix.LONG)
                /(!'I'.. arrayPreAtom%ArrayPrefix.INT)
            ).scope { it.getOrThrow(arrayPreAtom) }

            val intArrayEntriesAtom: Atom<List<IntegerLiteral>> = Atom.of("int_array_entries")
            intArrayEntriesAtom`=`(
                intArrayEntriesAtom%(intNumRule*0..!',')
            ).scope { it.getOrThrow(intArrayEntriesAtom) }

            val listLiteralAtom = Atom.of<T>("list_literal")
            listLiteralAtom `=` (
                !'['
                ..((this[arrayPreAtom]..!';'..this[intArrayEntriesAtom])
                    / this[listEntriesAtom])
                ..!']'
            ).state { parseState: ParseState ->
                val scope = parseState.scope()
                val arrayPrefix = scope[arrayPreAtom]
                if (arrayPrefix != null) {
                    val list = scope.getOrThrow(intArrayEntriesAtom)
                    return@state if (list.isEmpty()) arrayPrefix.create<T>(dynamicOps) else arrayPrefix.create<T>(
                        dynamicOps,
                        list,
                        parseState
                    )
                } else {
                    val list = scope.getOrThrow(listEntriesAtom)
                    return@state if (list.isEmpty()) emptyList else dynamicOps.createList(list.stream())
                }
            }
            val literalRule = literalAtom `=` (
                (+(SnbtGrammarUtils.NUMBER_LOOKEAHEAD)..(this[floatLiteralAtom to literalAtom]/ this[intLiteralAtom]))
                /(+(!('"'/ '\''))..cut()..this[quotedStrLiteralAtom])
                /(+(!'{')..cut()..this[mapLiteralAtom to literalAtom])
                /(+(!'[')..cut()..this[listLiteralAtom to literalAtom])
                /this[unquotedStrOrBuiltinAtom to literalAtom]
            ).state { parseState: ParseState ->
                val scope = parseState.scope()
                val string = scope[quotedStrLiteralAtom]
                if (string != null) {
                    return@state dynamicOps.createString(string)
                } else {
                    val integerLiteral = scope[intLiteralAtom]
                    return@state if (integerLiteral != null)
                        integerLiteral.create<T>(dynamicOps, parseState)
                    else scope.getOrThrow<T>(literalAtom)
                }
            }
            return@createParser literalRule
        }
        
        //return Grammar(dictionary, namedRule5)
    }

}