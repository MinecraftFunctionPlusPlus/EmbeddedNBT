package top.mcfpp.nbt.tags.collection;

import com.google.common.annotations.VisibleForTesting;
import org.jetbrains.annotations.Nullable;
import top.mcfpp.nbt.tags.CompoundTag;
import top.mcfpp.nbt.tags.Tag;
import top.mcfpp.nbt.tags.TagType;
import top.mcfpp.nbt.tags.primitive.NumericTag;
import top.mcfpp.nbt.tags.primitive.StringTag;
import top.mcfpp.nbt.visitors.StringTagVisitor;
import top.mcfpp.nbt.visitors.TagVisitor;
import top.mcfpp.utils.Functional;

import java.util.*;
import java.util.stream.Stream;

import static top.mcfpp.nbt.tags.CompoundTag.wrapElement;

public final class ListTag extends AbstractList<Tag> implements CollectionTag {
	private static final String WRAPPER_MARKER = "";
	private static final int SELF_SIZE_IN_BYTES = 36;
	public static final TagType<ListTag> TYPE = new TagType.VariableSize<ListTag>() {

		@Override
		public String getName() {
			return "LIST";
		}

		@Override
		public String getPrettyName() {
			return "TAG_List";
		}
	};
	private final List<Tag> list;

	public ListTag() {
		this(new ArrayList<>());
	}

	ListTag(List<Tag> list) {
		this.list = list;
	}

	public static ListTag fromStream(Stream<Tag> stream){
		 return new ListTag(stream.collect(Functional.toMutableList()));
	}


	private static Tag tryUnwrap(CompoundTag compoundTag) {
		if (compoundTag.size() == 1) {
			Tag tag = compoundTag.get(WRAPPER_MARKER);
			if (tag != null) {
				return tag;
			}
		}

		return compoundTag;
	}

	private static boolean isWrapper(CompoundTag compoundTag) {
		return compoundTag.size() == 1 && compoundTag.contains(WRAPPER_MARKER);
	}

	private static Tag wrapIfNeeded(byte b, Tag tag) {
		if (b != 10) {
			return tag;
		} else {
			return tag instanceof CompoundTag compoundTag && !isWrapper(compoundTag) ? compoundTag : wrapElement(tag);
		}
	}


	@VisibleForTesting
	byte identifyRawElementType() {
		byte b = 0;

		for (Tag tag : this.list) {
			byte c = tag.getId();
			if (b == 0) {
				b = c;
			} else if (b != c) {
				return 10;
			}
		}

		return b;
	}

	public void addAndUnwrap(Tag tag) {
		if (tag instanceof CompoundTag compoundTag) {
			this.add(tryUnwrap(compoundTag));
		} else {
			this.add(tag);
		}
	}

	@Override
	public int sizeInBytes() {
		int i = SELF_SIZE_IN_BYTES;
		i += 4 * this.list.size();

		for (Tag tag : this.list) {
			i += tag.sizeInBytes();
		}

		return i;
	}

	@Override
	public byte getId() {
		return TAG_LIST;
	}

	@Override
	public TagType<ListTag> getType() {
		return TYPE;
	}

	@Override
	public String toString() {
		StringTagVisitor stringTagVisitor = new StringTagVisitor();
		stringTagVisitor.visitList(this);
		return stringTagVisitor.build();
	}

	@Override
	public Tag remove(int i) {
		return this.list.remove(i);
	}

	@Override
	public boolean isEmpty() {
		return this.list.isEmpty();
	}

	public Optional<CompoundTag> getCompound(int i) {
		return this.getNullable(i) instanceof CompoundTag compoundTag ? Optional.of(compoundTag) : Optional.empty();
	}

	public CompoundTag getCompoundOrEmpty(int i) {
		return this.getCompound(i).orElseGet(CompoundTag::new);
	}

	public Optional<ListTag> getList(int i) {
		return this.getNullable(i) instanceof ListTag listTag ? Optional.of(listTag) : Optional.empty();
	}

	public ListTag getListOrEmpty(int i) {
		return this.getList(i).orElseGet(ListTag::new);
	}

	public Optional<Short> getShort(int i) {
		return this.getOptional(i).flatMap(Tag::asShort);
	}

	public short getShortOr(int i, short s) {
		return this.getNullable(i) instanceof NumericTag numericTag ? numericTag.shortValue() : s;
	}

	public Optional<Integer> getInt(int i) {
		return this.getOptional(i).flatMap(Tag::asInt);
	}

	public int getIntOr(int i, int j) {
		return this.getNullable(i) instanceof NumericTag numericTag ? numericTag.intValue() : j;
	}

	public Optional<int[]> getIntArray(int i) {
		return this.getNullable(i) instanceof IntArrayTag intArrayTag ? Optional.of(intArrayTag.getAsIntArray()) : Optional.empty();
	}

	public Optional<long[]> getLongArray(int i) {
		return this.getNullable(i) instanceof LongArrayTag longArrayTag ? Optional.of(longArrayTag.getAsLongArray()) : Optional.empty();
	}

	public Optional<Double> getDouble(int i) {
		return this.getOptional(i).flatMap(Tag::asDouble);
	}

	public double getDoubleOr(int i, double d) {
		return this.getNullable(i) instanceof NumericTag numericTag ? numericTag.doubleValue() : d;
	}

	public Optional<Float> getFloat(int i) {
		return this.getOptional(i).flatMap(Tag::asFloat);
	}

	public float getFloatOr(int i, float f) {
		return this.getNullable(i) instanceof NumericTag numericTag ? numericTag.floatValue() : f;
	}

	public Optional<String> getString(int i) {
		return this.getOptional(i).flatMap(Tag::asString);
	}

	public String getStringOr(int i, String string) {
		return this.getNullable(i) instanceof StringTag(String var8) ? var8 : string;
	}

	@Nullable
	private Tag getNullable(int i) {
		return i >= 0 && i < this.list.size() ? this.list.get(i) : null;
	}

	private Optional<Tag> getOptional(int i) {
		return Optional.ofNullable(this.getNullable(i));
	}

	@Override
	public int size() {
		return this.list.size();
	}

	@Override
	public Tag get(int i) {
		return this.list.get(i);
	}

	public Tag set(int i, Tag tag) {
		return this.list.set(i, tag);
	}

	public void add(int i, Tag tag) {
		this.list.add(i, tag);
	}

	@Override
	public boolean setTag(int i, Tag tag) {
		this.list.set(i, tag);
		return true;
	}

	@Override
	public boolean addTag(int i, Tag tag) {
		this.list.add(i, tag);
		return true;
	}

	public ListTag copy() {
		List<Tag> list = new ArrayList<>(this.list.size());

		for (Tag tag : this.list) {
			list.add(tag.copy());
		}

		return new ListTag(list);
	}

	@Override
	public Optional<ListTag> asList() {
		return Optional.of(this);
	}

	public boolean equals(Object object) {
		return this == object || object instanceof ListTag && Objects.equals(this.list, ((ListTag) object).list);
	}

	public int hashCode() {
		return this.list.hashCode();
	}

	@Override
	public Stream<Tag> stream() {
		return super.stream();
	}

	public Stream<CompoundTag> compoundStream() {
		return this.stream().mapMulti((tag, consumer) -> {
			if (tag instanceof CompoundTag compoundTag) {
				consumer.accept(compoundTag);
			}
		});
	}

	@Override
	public void accept(TagVisitor tagVisitor) {
		tagVisitor.visitList(this);
	}

	@Override
	public void clear() {
		this.list.clear();
	}
}
