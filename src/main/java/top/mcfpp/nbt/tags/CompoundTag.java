package top.mcfpp.nbt.tags;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.Nullable;
import top.mcfpp.nbt.NbtOps;
import top.mcfpp.nbt.tags.collection.ByteArrayTag;
import top.mcfpp.nbt.tags.collection.IntArrayTag;
import top.mcfpp.nbt.tags.collection.ListTag;
import top.mcfpp.nbt.tags.collection.LongArrayTag;
import top.mcfpp.nbt.tags.primitive.*;
import top.mcfpp.nbt.visitors.StringTagVisitor;
import top.mcfpp.nbt.visitors.TagVisitor;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

public final class CompoundTag implements Tag {
	private static final String WRAPPER_MARKER = "";
	private final Map<String, Tag> tags;

	public CompoundTag(Map<String, Tag> map) {
		this.tags = map;
	}

	public CompoundTag() {
		this(new HashMap<>());
	}

	public Set<String> keySet() {
		return this.tags.keySet();
	}

	public Set<Entry<String, Tag>> entrySet() {
		return this.tags.entrySet();
	}

	public Collection<Tag> values() {
		return this.tags.values();
	}

	public void forEach(BiConsumer<String, Tag> biConsumer) {
		this.tags.forEach(biConsumer);
	}

	public int size() {
		return this.tags.size();
	}

	public Tag put(String string, Object e){
		return switch (e){
			case Byte b -> this.tags.put(string, ByteTag.valueOf(b));
            case Short s -> this.tags.put(string, ShortTag.valueOf(s));
            case Integer i -> this.tags.put(string, IntTag.valueOf(i));
            case Long l -> this.tags.put(string, LongTag.valueOf(l));
            case Float f -> this.tags.put(string, FloatTag.valueOf(f));
            case Double d -> this.tags.put(string, DoubleTag.valueOf(d));
            case String s -> this.tags.put(string, StringTag.valueOf(s));
            case byte[] bs -> this.tags.put(string, new ByteArrayTag(bs));
            case int[] is -> this.tags.put(string, new IntArrayTag(is));
            case long[] ls -> this.tags.put(string, new LongArrayTag(ls));
            case Boolean b -> this.tags.put(string, ByteTag.valueOf(b));
			case Tag tag -> this.tags.put(string, tag);
            default -> throw new IllegalStateException("Unexpected value: " + e);
        };
	}

	@Nullable
	public Tag get(String string) {
		return this.tags.get(string);
	}

	public boolean contains(String string) {
		return this.tags.containsKey(string);
	}


	public <T> Optional<T> get(String key, Class<T> type) {

		Tag tag = this.tags.get(key);
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
			case IntArrayTag intArrayTag when type == int[].class
					-> Optional.of(type.cast(intArrayTag.getAsIntArray()));
			case LongArrayTag longArrayTag when type == long[].class
					-> Optional.of(type.cast(longArrayTag.getAsLongArray()));
			case NumericTag numericTag when type == double.class
					-> Optional.of(type.cast(numericTag.doubleValue()));
			case NumericTag numericTag when type == float.class
					-> Optional.of(type.cast(numericTag.floatValue()));
			case StringTag stringTag when type == String.class
					-> Optional.of(type.cast(stringTag.asString()));
			default -> Optional.empty();
		};
	}

	public <T> T getOrDefault(String key, T t, Class<T> type) {
		return this.get(key, type).orElse(t);
	}

	public Optional<Tag> getOptional(String string) {
		return Optional.ofNullable(this.tags.get(string));
	}

	public CompoundTag getCompoundOrEmpty(String key) {
		return this.getOrDefault(key, new CompoundTag(), CompoundTag.class);
	}

	public ListTag getListOrEmpty(String  key) {
		return this.getOrDefault(key, new ListTag(), ListTag.class);
	}

	public void remove(String string) {
		this.tags.remove(string);
	}

	@Override
	public String toString() {
		StringTagVisitor stringTagVisitor = new StringTagVisitor();
		stringTagVisitor.visitCompound(this);
		return stringTagVisitor.build();
	}

	public boolean isEmpty() {
		return this.tags.isEmpty();
	}

	public CompoundTag shallowCopy() {
		return new CompoundTag(new HashMap<>(this.tags));
	}

	public CompoundTag copy() {
		HashMap<String, Tag> hashMap = new HashMap<>();
		this.tags.forEach((string, tag) -> hashMap.put(string, tag.copy()));
		return new CompoundTag(hashMap);
	}

	@Override
	public Optional<CompoundTag> asCompound() {
		return Optional.of(this);
	}

	public boolean equals(Object object) {
		return this == object || object instanceof CompoundTag && Objects.equals(this.tags, ((CompoundTag) object).tags);
	}

	public int hashCode() {
		return this.tags.hashCode();
	}


	public CompoundTag merge(CompoundTag compoundTag) {
		for (String string : compoundTag.tags.keySet()) {
			Tag tag = compoundTag.tags.get(string);
			if (tag instanceof CompoundTag compoundTag2 && this.tags.get(string) instanceof CompoundTag compoundTag3) {
				compoundTag3.merge(compoundTag2);
			} else {
				this.put(string, tag.copy());
			}
		}

		return this;
	}

	@Override
	public void accept(TagVisitor tagVisitor) {
		tagVisitor.visitCompound(this);
	}

	public <T> void store(String string, Codec<T> codec, T object) {
		this.store(string, codec, NbtOps.INSTANCE, object);
	}

	public <T> void storeNullable(String string, Codec<T> codec, @Nullable T object) {
		if (object != null) {
			this.store(string, codec, object);
		}
	}

	public <T> void store(String string, Codec<T> codec, DynamicOps<Tag> dynamicOps, T object) {
		this.put(string, codec.encodeStart(dynamicOps, object).getOrThrow());
	}

	public <T> void storeNullable(String string, Codec<T> codec, DynamicOps<Tag> dynamicOps, @Nullable T object) {
		if (object != null) {
			this.store(string, codec, dynamicOps, object);
		}
	}

	public <T> void store(MapCodec<T> mapCodec, T object) {
		this.store(mapCodec, NbtOps.INSTANCE, object);
	}

	public <T> void store(MapCodec<T> mapCodec, DynamicOps<Tag> dynamicOps, T object) {
		this.merge((CompoundTag)mapCodec.encoder().encodeStart(dynamicOps, object).getOrThrow());
	}

	public <T> Optional<T> read(String string, Codec<T> codec) {
		return this.read(string, codec, NbtOps.INSTANCE);
	}

	public <T> Optional<T> read(String string, Codec<T> codec, DynamicOps<Tag> dynamicOps) {
		Tag tag = this.get(string);
		return tag == null
			? Optional.empty()
			: codec.parse(dynamicOps, tag).resultOrPartial(string2 -> /*LOGGER.error("Failed to read field ({}={}): {}", string, tag, string2)*/{});
	}

	public <T> Optional<T> read(MapCodec<T> mapCodec) {
		return this.read(mapCodec, NbtOps.INSTANCE);
	}

	public <T> Optional<T> read(MapCodec<T> mapCodec, DynamicOps<Tag> dynamicOps) {
		return mapCodec.decode(dynamicOps, dynamicOps.getMap(this).getOrThrow())
			.resultOrPartial(string -> /*LOGGER.error("Failed to read value ({}): {}", this, string)*/{});
	}
}
