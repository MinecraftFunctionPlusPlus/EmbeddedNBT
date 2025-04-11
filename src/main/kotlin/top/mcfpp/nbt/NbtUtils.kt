package top.mcfpp.nbt;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.jetbrains.annotations.Nullable;
import top.mcfpp.nbt.parsers.Parser;
import top.mcfpp.nbt.tags.CompoundTag;
import top.mcfpp.nbt.tags.Tag;
import top.mcfpp.nbt.tags.collection.ListTag;
import top.mcfpp.nbt.visitors.SnbtPrinterTagVisitor;
import top.mcfpp.nbt.visitors.SnbtTagVisitor;

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

	public static String nbtToSnbtPretty(CompoundTag compoundTag) {
		return new SnbtPrinterTagVisitor().visit(compoundTag);
	}

	public static String nbtToSnbt(CompoundTag compoundTag) {
		return new SnbtTagVisitor().visit(compoundTag);
	}

	public static CompoundTag snbtToNbt(String string) throws CommandSyntaxException {
		return Parser.parseCompoundFully(string);
	}

}

