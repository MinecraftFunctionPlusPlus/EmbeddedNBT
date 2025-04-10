package top.mcfpp.nbt.tags.collection;

import top.mcfpp.nbt.tags.CompoundTag;
import top.mcfpp.nbt.tags.Tag;
import top.mcfpp.nbt.tags.primitive.ByteTag;
import top.mcfpp.nbt.tags.primitive.NumericTag;
import top.mcfpp.nbt.tags.primitive.StringTag;
import top.mcfpp.nbt.visitors.StringTagVisitor;
import top.mcfpp.nbt.visitors.TagVisitor;
import top.mcfpp.utils.Functional;

import java.util.*;
import java.util.stream.Stream;

@SuppressWarnings({"unused", "NullableProblems"})
public final class ListTag extends AbstractList<Tag> implements CollectionTag {

	private static final String WRAPPER_MARKER = "";
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

	public <T> Optional<T> get(int i, Class<T> type) {

		Tag tag = this.list.get(i);
		if (tag == null) {
			return Optional.empty();
		}

		return switch (tag) {
			case CompoundTag compoundTag when type == CompoundTag.class
					-> Optional.of(type.cast(compoundTag));
			case ListTag listTag when type == ListTag.class
					-> Optional.of(type.cast(listTag));
			case ByteTag byteTag when type == byte.class
					-> Optional.of(type.cast(byteTag.byteValue()));
			case NumericTag numericTag when type == short.class
					-> Optional.of(type.cast(numericTag.shortValue()));
			case NumericTag numericTag when type == int.class
					-> Optional.of(type.cast(numericTag.intValue()));
			case IntArrayTag tags when type == int[].class
					-> Optional.of(type.cast(tags.getAsIntArray()));
			case LongArrayTag tags when type == long[].class
					-> Optional.of(type.cast(tags.getAsLongArray()));
			case NumericTag numericTag when type == double.class
					-> Optional.of(type.cast(numericTag.doubleValue()));
			case NumericTag numericTag when type == float.class
					-> Optional.of(type.cast(numericTag.floatValue()));
			case StringTag stringTag when type == String.class
					-> Optional.of(type.cast(stringTag.asString()));
			default -> Optional.empty();
		};
	}

	public <T> T getOrDefault(int i, T t, Class<T> type) {
		return this.get(i, type).orElse(t);
	}


	public CompoundTag getCompoundOrEmpty(int i) {
		return this.getOrDefault(i, new CompoundTag(), CompoundTag.class);
	}

	public ListTag getListOrEmpty(int i) {
		return this.getOrDefault(i, new ListTag(), ListTag.class);
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

	@Override
	public void accept(TagVisitor tagVisitor) {
		tagVisitor.visitList(this);
	}

	@Override
	public void clear() {
		this.list.clear();
	}

	public boolean NeedWrap() {
		if(list.size() <= 1) return false;
		Tag tag = list.getFirst();
		return list.stream().anyMatch(t -> !t.getClass().equals(tag.getClass()));
	}

	public ListTag getWrappedList() {
		if(!NeedWrap()) return this;
		ListTag listTag = new ListTag();
		for (Tag tag : list) {
			listTag.add(new CompoundTag(Map.of("", tag)));
		}
		return listTag;
	}
}
