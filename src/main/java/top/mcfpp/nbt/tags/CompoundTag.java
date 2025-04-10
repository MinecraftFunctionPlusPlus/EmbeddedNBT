package top.mcfpp.nbt.tags;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.Nullable;
import top.mcfpp.nbt.*;
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
	public static final Codec<CompoundTag> CODEC = Codec.PASSTHROUGH
		.comapFlatMap(
			dynamic -> {
				Tag tag = dynamic.convert(NbtOps.INSTANCE).getValue();
				return tag instanceof CompoundTag compoundTag
					? DataResult.success(compoundTag == dynamic.getValue() ? compoundTag.copy() : compoundTag)
					: DataResult.error(() -> "Not a compound tag: " + tag);
			},
			compoundTag -> new Dynamic<>(NbtOps.INSTANCE, compoundTag.copy())
		);
	private static final int SELF_SIZE_IN_BYTES = 48;
	private static final int MAP_ENTRY_SIZE_IN_BYTES = 32;
	public static final TagType<CompoundTag> TYPE = new TagType.VariableSize<CompoundTag>() {

		@Override
		public String getName() {
			return "COMPOUND";
		}

		@Override
		public String getPrettyName() {
			return "TAG_Compound";
		}
	};
	private final Map<String, Tag> tags;

	CompoundTag(Map<String, Tag> map) {
		this.tags = map;
	}

	public static CompoundTag wrapElement(Tag tag) {
		return new CompoundTag(Map.of(WRAPPER_MARKER, tag));
	}

	public CompoundTag() {
		this(new HashMap<>());
	}


	@Override
	public int sizeInBytes() {
		int i = SELF_SIZE_IN_BYTES;

		for (Entry<String, Tag> entry : this.tags.entrySet()) {
			i += STRING_SIZE + 2 * entry.getKey().length();
			i += 36;
			i += entry.getValue().sizeInBytes();
		}

		return i;
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

	@Override
	public byte getId() {
		return TAG_COMPOUND;
	}

	@Override
	public TagType<CompoundTag> getType() {
		return TYPE;
	}

	public int size() {
		return this.tags.size();
	}

	@Nullable
	public Tag put(String string, Tag tag) {
		return this.tags.put(string, tag);
	}

	public void putByte(String string, byte b) {
		this.tags.put(string, ByteTag.valueOf(b));
	}

	public void putShort(String string, short s) {
		this.tags.put(string, ShortTag.valueOf(s));
	}

	public void putInt(String string, int i) {
		this.tags.put(string, IntTag.valueOf(i));
	}

	public void putLong(String string, long l) {
		this.tags.put(string, LongTag.valueOf(l));
	}

	public void putFloat(String string, float f) {
		this.tags.put(string, FloatTag.valueOf(f));
	}

	public void putDouble(String string, double d) {
		this.tags.put(string, DoubleTag.valueOf(d));
	}

	public void putString(String string, String string2) {
		this.tags.put(string, StringTag.valueOf(string2));
	}

	public void putByteArray(String string, byte[] bs) {
		this.tags.put(string, new ByteArrayTag(bs));
	}

	public void putIntArray(String string, int[] is) {
		this.tags.put(string, new IntArrayTag(is));
	}

	public void putLongArray(String string, long[] ls) {
		this.tags.put(string, new LongArrayTag(ls));
	}

	public void putBoolean(String string, boolean bl) {
		this.tags.put(string, ByteTag.valueOf(bl));
	}

	@Nullable
	public Tag get(String string) {
		return this.tags.get(string);
	}

	public boolean contains(String string) {
		return this.tags.containsKey(string);
	}

	private Optional<Tag> getOptional(String string) {
		return Optional.ofNullable(this.tags.get(string));
	}

	public Optional<Byte> getByte(String string) {
		return this.getOptional(string).flatMap(Tag::asByte);
	}

	public byte getByteOr(String string, byte b) {
		return this.tags.get(string) instanceof NumericTag numericTag ? numericTag.byteValue() : b;
	}

	public Optional<Short> getShort(String string) {
		return this.getOptional(string).flatMap(Tag::asShort);
	}

	public short getShortOr(String string, short s) {
		return this.tags.get(string) instanceof NumericTag numericTag ? numericTag.shortValue() : s;
	}

	public Optional<Integer> getInt(String string) {
		return this.getOptional(string).flatMap(Tag::asInt);
	}

	public int getIntOr(String string, int i) {
		return this.tags.get(string) instanceof NumericTag numericTag ? numericTag.intValue() : i;
	}

	public Optional<Long> getLong(String string) {
		return this.getOptional(string).flatMap(Tag::asLong);
	}

	public long getLongOr(String string, long l) {
		return this.tags.get(string) instanceof NumericTag numericTag ? numericTag.longValue() : l;
	}

	public Optional<Float> getFloat(String string) {
		return this.getOptional(string).flatMap(Tag::asFloat);
	}

	public float getFloatOr(String string, float f) {
		return this.tags.get(string) instanceof NumericTag numericTag ? numericTag.floatValue() : f;
	}

	public Optional<Double> getDouble(String string) {
		return this.getOptional(string).flatMap(Tag::asDouble);
	}

	public double getDoubleOr(String string, double d) {
		return this.tags.get(string) instanceof NumericTag numericTag ? numericTag.doubleValue() : d;
	}

	public Optional<String> getString(String string) {
		return this.getOptional(string).flatMap(Tag::asString);
	}

	public String getStringOr(String string, String string2) {
		return this.tags.get(string) instanceof StringTag(String var8) ? var8 : string2;
	}

	public Optional<byte[]> getByteArray(String string) {
		return this.tags.get(string) instanceof ByteArrayTag byteArrayTag ? Optional.of(byteArrayTag.getAsByteArray()) : Optional.empty();
	}

	public Optional<int[]> getIntArray(String string) {
		return this.tags.get(string) instanceof IntArrayTag intArrayTag ? Optional.of(intArrayTag.getAsIntArray()) : Optional.empty();
	}

	public Optional<long[]> getLongArray(String string) {
		return this.tags.get(string) instanceof LongArrayTag longArrayTag ? Optional.of(longArrayTag.getAsLongArray()) : Optional.empty();
	}

	public Optional<CompoundTag> getCompound(String string) {
		return this.tags.get(string) instanceof CompoundTag compoundTag ? Optional.of(compoundTag) : Optional.empty();
	}

	public CompoundTag getCompoundOrEmpty(String string) {
		return this.getCompound(string).orElseGet(CompoundTag::new);
	}

	public Optional<ListTag> getList(String string) {
		return this.tags.get(string) instanceof ListTag listTag ? Optional.of(listTag) : Optional.empty();
	}

	public ListTag getListOrEmpty(String string) {
		return this.getList(string).orElseGet(ListTag::new);
	}

	public Optional<Boolean> getBoolean(String string) {
		return this.getOptional(string).flatMap(Tag::asBoolean);
	}

	public boolean getBooleanOr(String string, boolean bl) {
		return this.getByteOr(string, (byte)(bl ? 1 : 0)) != 0;
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
		return new CompoundTag(new HashMap(this.tags));
	}

	public CompoundTag copy() {
		HashMap<String, Tag> hashMap = new HashMap();
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
