package top.mcfpp.nbt;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.jetbrains.annotations.Nullable;
import top.mcfpp.nbt.parsers.Parser;
import top.mcfpp.nbt.tags.CompoundTag;
import top.mcfpp.nbt.tags.Tag;
import top.mcfpp.nbt.tags.collection.ByteArrayTag;
import top.mcfpp.nbt.tags.collection.IntArrayTag;
import top.mcfpp.nbt.tags.collection.ListTag;
import top.mcfpp.nbt.tags.collection.LongArrayTag;
import top.mcfpp.nbt.tags.EndTag;
import top.mcfpp.nbt.tags.primitive.PrimitiveTag;
import top.mcfpp.nbt.visitors.SnbtPrinterTagVisitor;

import java.util.*;
import java.util.Map.Entry;

public final class NbtUtils {
	private NbtUtils() {}

	@VisibleForTesting
	public static boolean compareNbt(@Nullable Tag tag, @Nullable Tag tag2, boolean bl) {
		if (tag == tag2) {
			return true;
		} else if (tag == null) {
			return true;
		} else if (tag2 == null) {
			return false;
		} else if (!tag.getClass().equals(tag2.getClass())) {
			return false;
		} else if (tag instanceof CompoundTag compoundTag) {
			CompoundTag compoundTag2 = (CompoundTag)tag2;
			if (compoundTag2.size() < compoundTag.size()) {
				return false;
			} else {
				for (Entry<String, Tag> entry : compoundTag.entrySet()) {
					Tag tag3 = entry.getValue();
					if (!compareNbt(tag3, compoundTag2.get(entry.getKey()), bl)) {
						return false;
					}
				}

				return true;
			}
		} else if (tag instanceof ListTag listTag && bl) {
			ListTag listTag2 = (ListTag)tag2;
			if (listTag.isEmpty()) {
				return listTag2.isEmpty();
			} else if (listTag2.size() < listTag.size()) {
				return false;
			} else {
				for (Tag tag4 : listTag) {
					boolean bl2 = false;

					for (Tag tag5 : listTag2) {
						if (compareNbt(tag4, tag5, bl)) {
							bl2 = true;
							break;
						}
					}

					if (!bl2) {
						return false;
					}
				}

				return true;
			}
		} else {
			return tag.equals(tag2);
		}
	}

	public static String prettyPrint(Tag tag) {
		return prettyPrint(tag, false);
	}

	public static String prettyPrint(Tag tag, boolean bl) {
		return prettyPrint(new StringBuilder(), tag, 0, bl).toString();
	}

	public static StringBuilder prettyPrint(StringBuilder stringBuilder, Tag tag, int i, boolean bl) {
		return switch (tag) {
			case PrimitiveTag primitiveTag -> stringBuilder.append(primitiveTag);
			case EndTag endTag -> stringBuilder;
			case ByteArrayTag byteArrayTag -> {
				byte[] bs = byteArrayTag.getAsByteArray();
				int j = bs.length;
				indent(i, stringBuilder).append("byte[").append(j).append("] {\n");
				if (bl) {
					indent(i + 1, stringBuilder);

					for (int k = 0; k < bs.length; k++) {
						if (k != 0) {
							stringBuilder.append(',');
						}

						if (k % 16 == 0 && k / 16 > 0) {
							stringBuilder.append('\n');
                            indent(i + 1, stringBuilder);
                        } else if (k != 0) {
							stringBuilder.append(' ');
						}

						stringBuilder.append(String.format(Locale.ROOT, "0x%02X", bs[k] & 255));
					}
				} else {
					indent(i + 1, stringBuilder).append(" // Skipped, supply withBinaryBlobs true");
				}

				stringBuilder.append('\n');
				indent(i, stringBuilder).append('}');
				yield stringBuilder;
			}
			case ListTag listTag -> {
				int j = listTag.size();
				indent(i, stringBuilder).append("list").append("[").append(j).append("] [");
				if (j != 0) {
					stringBuilder.append('\n');
				}

				for (int k = 0; k < j; k++) {
					if (k != 0) {
						stringBuilder.append(",\n");
					}

					indent(i + 1, stringBuilder);
					prettyPrint(stringBuilder, listTag.get(k), i + 1, bl);
				}

				if (j != 0) {
					stringBuilder.append('\n');
				}

				indent(i, stringBuilder).append(']');
				yield stringBuilder;
			}
			case IntArrayTag intArrayTag -> {
				int[] is = intArrayTag.getAsIntArray();
				int l = 0;

				for (int m : is) {
					l = Math.max(l, String.format(Locale.ROOT, "%X", m).length());
				}

				int n = is.length;
				indent(i, stringBuilder).append("int[").append(n).append("] {\n");
				if (bl) {
					indent(i + 1, stringBuilder);

					for (int o = 0; o < is.length; o++) {
						if (o != 0) {
							stringBuilder.append(',');
						}

						if (o % 16 == 0 && o / 16 > 0) {
							stringBuilder.append('\n');
                            indent(i + 1, stringBuilder);
                        } else if (o != 0) {
							stringBuilder.append(' ');
						}

						stringBuilder.append(String.format(Locale.ROOT, "0x%0" + l + "X", is[o]));
					}
				} else {
					indent(i + 1, stringBuilder).append(" // Skipped, supply withBinaryBlobs true");
				}

				stringBuilder.append('\n');
				indent(i, stringBuilder).append('}');
				yield stringBuilder;
			}
			case CompoundTag compoundTag -> {
				List<String> list = Lists.newArrayList(compoundTag.keySet());
				Collections.sort(list);
				indent(i, stringBuilder).append('{');
				if (stringBuilder.length() - stringBuilder.lastIndexOf("\n") > 2 * (i + 1)) {
					stringBuilder.append('\n');
					indent(i + 1, stringBuilder);
				}

				int n = list.stream().mapToInt(String::length).max().orElse(0);
				String string = Strings.repeat(" ", n);

				for (int p = 0; p < list.size(); p++) {
					if (p != 0) {
						stringBuilder.append(",\n");
					}

					String string2 = list.get(p);
					indent(i + 1, stringBuilder).append('"').append(string2).append('"').append(string, 0, string.length() - string2.length()).append(": ");
					prettyPrint(stringBuilder, compoundTag.get(string2), i + 1, bl);
				}

				if (!list.isEmpty()) {
					stringBuilder.append('\n');
				}

				indent(i, stringBuilder).append('}');
				yield stringBuilder;
			}
			case LongArrayTag longArrayTag -> {
				long[] ls = longArrayTag.getAsLongArray();
				long q = 0L;

				for (long r : ls) {
					q = Math.max(q, String.format(Locale.ROOT, "%X", r).length());
				}

				long s = ls.length;
				indent(i, stringBuilder).append("long[").append(s).append("] {\n");
				if (bl) {
					indent(i + 1, stringBuilder);

					for (int t = 0; t < ls.length; t++) {
						if (t != 0) {
							stringBuilder.append(',');
						}

						if (t % 16 == 0 && t / 16 > 0) {
							stringBuilder.append('\n');
                            indent(i + 1, stringBuilder);
                        } else if (t != 0) {
							stringBuilder.append(' ');
						}

						stringBuilder.append(String.format(Locale.ROOT, "0x%0" + q + "X", ls[t]));
					}
				} else {
					indent(i + 1, stringBuilder).append(" // Skipped, supply withBinaryBlobs true");
				}

				stringBuilder.append('\n');
				indent(i, stringBuilder).append('}');
				yield stringBuilder;
			}
			default -> throw new MatchException(null, null);
		};
	}

	private static StringBuilder indent(int i, StringBuilder stringBuilder) {
		int j = stringBuilder.lastIndexOf("\n") + 1;
		int k = stringBuilder.length() - j;

        stringBuilder.append(" ".repeat(Math.max(0, 2 * i - k)));

		return stringBuilder;
	}

	public static String nbtToSnbt(CompoundTag compoundTag) {
		return new SnbtPrinterTagVisitor().visit(compoundTag);
	}

	public static CompoundTag snbtToNbt(String string) throws CommandSyntaxException {
		return Parser.parseCompoundFully(string);
	}

}
