package top.mcfpp.nbt.tags.primitive;

import top.mcfpp.nbt.tags.TagType;
import top.mcfpp.nbt.visitors.StringTagVisitor;
import top.mcfpp.nbt.visitors.TagVisitor;

import java.util.Optional;

import static top.mcfpp.nbt.parsers.SnbtGrammarUtils.escapeControlCharacters;

public record StringTag(String value) implements PrimitiveTag {
	private static final int SELF_SIZE_IN_BYTES = 36;
	public static final TagType<StringTag> TYPE = new TagType.VariableSize<StringTag>() {
		@Override
		public String getName() {
			return "STRING";
		}

		@Override
		public String getPrettyName() {
			return "TAG_String";
		}
	};
	private static final StringTag EMPTY = new StringTag("");
	private static final char DOUBLE_QUOTE = '"';
	private static final char SINGLE_QUOTE = '\'';
	private static final char ESCAPE = '\\';
	private static final char NOT_SET = '\u0000';

    @Deprecated(
            forRemoval = true
    )
    public StringTag {
    }


	public static StringTag valueOf(String string) {
		return string.isEmpty() ? EMPTY : new StringTag(string);
	}


	@Override
	public int sizeInBytes() {
		return SELF_SIZE_IN_BYTES + 2 * this.value.length();
	}

	@Override
	public byte getId() {
		return TAG_STRING;
	}

	@Override
	public TagType<StringTag> getType() {
		return TYPE;
	}

	@Override
	public String toString() {
		StringTagVisitor stringTagVisitor = new StringTagVisitor();
		stringTagVisitor.visitString(this);
		return stringTagVisitor.build();
	}

	public StringTag copy() {
		return this;
	}

	@Override
	public Optional<String> asString() {
		return Optional.of(this.value);
	}

	@Override
	public void accept(TagVisitor tagVisitor) {
		tagVisitor.visitString(this);
	}

	public static String quoteAndEscape(String string) {
		StringBuilder stringBuilder = new StringBuilder();
		quoteAndEscape(string, stringBuilder);
		return stringBuilder.toString();
	}

	public static void quoteAndEscape(String string, StringBuilder stringBuilder) {
		int i = stringBuilder.length();
		stringBuilder.append(' ');
		char c = 0;

		for (int j = 0; j < string.length(); j++) {
			char d = string.charAt(j);
			if (d == ESCAPE) {
				stringBuilder.append("\\\\");
			} else if (d != DOUBLE_QUOTE && d != SINGLE_QUOTE) {
				String string2 = escapeControlCharacters(d);
				if (string2 != null) {
					stringBuilder.append(ESCAPE);
					stringBuilder.append(string2);
				} else {
					stringBuilder.append(d);
				}
			} else {
				if (c == 0) {
					c = (char)(d == DOUBLE_QUOTE ? 39 : 34);
				}

				if (c == d) {
					stringBuilder.append(ESCAPE);
				}

				stringBuilder.append(d);
			}
		}

		if (c == 0) {
			c = DOUBLE_QUOTE;
		}

		stringBuilder.setCharAt(i, c);
		stringBuilder.append(c);
	}

}
