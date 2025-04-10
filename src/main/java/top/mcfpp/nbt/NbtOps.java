package top.mcfpp.nbt;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.RecordBuilder.AbstractStringBuilder;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import top.mcfpp.nbt.tags.CompoundTag;
import top.mcfpp.nbt.tags.EndTag;
import top.mcfpp.nbt.tags.Tag;
import top.mcfpp.nbt.tags.collection.*;
import top.mcfpp.nbt.tags.primitive.*;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class NbtOps implements DynamicOps<Tag> {
	public static final NbtOps INSTANCE = new NbtOps();

	private NbtOps() {
	}

	public Tag empty() {
		return EndTag.INSTANCE;
	}

	public <U> U convertTo(DynamicOps<U> dynamicOps, Tag tag) {
		Object u = switch (tag) {
			case EndTag endTag -> dynamicOps.empty();
			case ByteTag(byte b) -> dynamicOps.createByte(b);
			case ShortTag(short i) -> dynamicOps.createShort(i);
			case IntTag(int i) -> dynamicOps.createInt(i);
			case LongTag(long l) -> dynamicOps.createLong(l);
			case FloatTag(float f) -> dynamicOps.createFloat(f);
			case DoubleTag(double d) -> dynamicOps.createDouble(d);
			case ByteArrayTag byteArrayTag ->
					dynamicOps.createByteList(ByteBuffer.wrap(byteArrayTag.getAsByteArray()));
			case StringTag(String s) -> dynamicOps.createString(s);
			case ListTag listTag -> this.convertList(dynamicOps, listTag);
			case CompoundTag compoundTag -> this.convertMap(dynamicOps, compoundTag);
			case IntArrayTag intArrayTag ->
					dynamicOps.createIntList(Arrays.stream(intArrayTag.getAsIntArray()));
			case LongArrayTag longArrayTag ->
					dynamicOps.createLongList(Arrays.stream(longArrayTag.getAsLongArray()));
			default -> throw new MatchException(null, null);
		};
		return (U)u;
	}

	public DataResult<Number> getNumberValue(Tag tag) {
		return tag.asNumber().map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Not a number"));
	}

	public Tag createNumeric(Number number) {
		return DoubleTag.valueOf(number.doubleValue());
	}

	public Tag createByte(byte b) {
		return ByteTag.valueOf(b);
	}

	public Tag createShort(short s) {
		return ShortTag.valueOf(s);
	}

	public Tag createInt(int i) {
		return IntTag.valueOf(i);
	}

	public Tag createLong(long l) {
		return LongTag.valueOf(l);
	}

	public Tag createFloat(float f) {
		return FloatTag.valueOf(f);
	}

	public Tag createDouble(double d) {
		return DoubleTag.valueOf(d);
	}

	public Tag createBoolean(boolean bl) {
		return ByteTag.valueOf(bl);
	}

	public DataResult<String> getStringValue(Tag tag) {
		return tag instanceof StringTag(String var4) ? DataResult.success(var4) : DataResult.error(() -> "Not a string");
	}

	public Tag createString(String string) {
		return StringTag.valueOf(string);
	}

	public DataResult<Tag> mergeToList(Tag tag, Tag tag2) {
		return createCollector(tag)
			.map(listCollector -> DataResult.success(listCollector.accept(tag2).result()))
			.orElseGet(() -> DataResult.error(() -> "mergeToList called with not a list: " + tag, tag));
	}

	public DataResult<Tag> mergeToList(Tag tag, List<Tag> list) {
		return createCollector(tag)
			.map(listCollector -> DataResult.success(listCollector.acceptAll(list).result()))
			.orElseGet(() -> DataResult.error(() -> "mergeToList called with not a list: " + tag, tag));
	}

	public DataResult<Tag> mergeToMap(Tag tag, Tag tag2, Tag tag3) {
		if (!(tag instanceof CompoundTag) && !(tag instanceof EndTag)) {
			return DataResult.error(() -> "mergeToMap called with not a map: " + tag, tag);
		} else if (tag2 instanceof StringTag(String var10)) {
			String compoundTag = var10;
			CompoundTag compoundTag2 = tag instanceof CompoundTag compoundTagx ? compoundTagx.shallowCopy() : new CompoundTag();
			compoundTag2.put(compoundTag, tag3);
			return DataResult.success(compoundTag2);
		} else {
			return DataResult.error(() -> "key is not a string: " + tag2, tag);
		}
	}

	public DataResult<Tag> mergeToMap(Tag tag, MapLike<Tag> mapLike) {
		if (!(tag instanceof CompoundTag) && !(tag instanceof EndTag)) {
			return DataResult.error(() -> "mergeToMap called with not a map: " + tag, tag);
		} else {
			CompoundTag compoundTag2 = tag instanceof CompoundTag compoundTag ? compoundTag.shallowCopy() : new CompoundTag();
			List<Tag> list = new ArrayList<>();
			mapLike.entries().forEach(pair -> {
				Tag tagx = pair.getFirst();
				if (tagx instanceof StringTag(String string)) {
					compoundTag2.put(string, pair.getSecond());
				} else {
					list.add(tagx);
				}
			});
			return !list.isEmpty() ? DataResult.error(() -> "some keys are not strings: " + list, compoundTag2) : DataResult.success(compoundTag2);
		}
	}

	public DataResult<Tag> mergeToMap(Tag tag, Map<Tag, Tag> map) {
		if (!(tag instanceof CompoundTag) && !(tag instanceof EndTag)) {
			return DataResult.error(() -> "mergeToMap called with not a map: " + tag, tag);
		} else {
			CompoundTag compoundTag2 = tag instanceof CompoundTag compoundTag ? compoundTag.shallowCopy() : new CompoundTag();
			List<Tag> list = new ArrayList<>();

			for (Entry<Tag, Tag> entry : map.entrySet()) {
				Tag tag2 = entry.getKey();
				if (tag2 instanceof StringTag(String var10)) {
					compoundTag2.put(var10, entry.getValue());
				} else {
					list.add(tag2);
				}
			}

			return !list.isEmpty() ? DataResult.error(() -> "some keys are not strings: " + list, compoundTag2) : DataResult.success(compoundTag2);
		}
	}

	public DataResult<Stream<Pair<Tag, Tag>>> getMapValues(Tag tag) {
		return tag instanceof CompoundTag compoundTag
			? DataResult.success(compoundTag.entrySet().stream().map(entry -> Pair.of(this.createString(entry.getKey()), entry.getValue())))
			: DataResult.error(() -> "Not a map: " + tag);
	}

	public DataResult<Consumer<BiConsumer<Tag, Tag>>> getMapEntries(Tag tag) {
		return tag instanceof CompoundTag compoundTag ? DataResult.success(biConsumer -> {
			for (Entry<String, Tag> entry : compoundTag.entrySet()) {
				biConsumer.accept(this.createString(entry.getKey()), entry.getValue());
			}
		}) : DataResult.error(() -> "Not a map: " + tag);
	}

	public DataResult<MapLike<Tag>> getMap(Tag tag) {
		return tag instanceof CompoundTag compoundTag ? DataResult.success(new MapLike<Tag>() {
			@Nullable
			public Tag get(Tag tag) {
				if (tag instanceof StringTag(String var4)) {
					return compoundTag.get(var4);
				} else {
					throw new UnsupportedOperationException("Cannot get map entry with non-string key: " + tag);
				}
			}

			@Nullable
			public Tag get(String string) {
				return compoundTag.get(string);
			}

			@Override
			public Stream<Pair<Tag, Tag>> entries() {
				return compoundTag.entrySet().stream().map(entry -> Pair.of(NbtOps.this.createString(entry.getKey()), entry.getValue()));
			}

			public String toString() {
				return "MapLike[" + compoundTag + "]";
			}
		}) : DataResult.error(() -> "Not a map: " + tag);
	}

	public Tag createMap(Stream<Pair<Tag, Tag>> stream) {
		CompoundTag compoundTag = new CompoundTag();
		stream.forEach(pair -> {
			Tag tag = pair.getFirst();
			Tag tag2 = pair.getSecond();
			if (tag instanceof StringTag(String string)) {
				compoundTag.put(string, tag2);
			} else {
				throw new UnsupportedOperationException("Cannot create map with non-string key: " + tag);
			}
		});
		return compoundTag;
	}

	public DataResult<Stream<Tag>> getStream(Tag tag) {
		return tag instanceof CollectionTag collectionTag ? DataResult.success(collectionTag.stream()) : DataResult.error(() -> "Not a list");
	}

	public DataResult<Consumer<Consumer<Tag>>> getList(Tag tag) {
		return tag instanceof CollectionTag collectionTag ? DataResult.success(collectionTag::forEach) : DataResult.error(() -> "Not a list: " + tag);
	}

	public DataResult<ByteBuffer> getByteBuffer(Tag tag) {
		return tag instanceof ByteArrayTag byteArrayTag ? DataResult.success(ByteBuffer.wrap(byteArrayTag.getAsByteArray())) : DynamicOps.super.getByteBuffer(tag);
	}

	public Tag createByteList(ByteBuffer byteBuffer) {
		ByteBuffer byteBuffer2 = byteBuffer.duplicate().clear();
		byte[] bs = new byte[byteBuffer.capacity()];
		byteBuffer2.get(0, bs, 0, bs.length);
		return new ByteArrayTag(bs);
	}

	public DataResult<IntStream> getIntStream(Tag tag) {
		return tag instanceof IntArrayTag intArrayTag ? DataResult.success(Arrays.stream(intArrayTag.getAsIntArray())) : DynamicOps.super.getIntStream(tag);
	}

	public Tag createIntList(IntStream intStream) {
		return new IntArrayTag(intStream.toArray());
	}

	public DataResult<LongStream> getLongStream(Tag tag) {
		return tag instanceof LongArrayTag longArrayTag ? DataResult.success(Arrays.stream(longArrayTag.getAsLongArray())) : DynamicOps.super.getLongStream(tag);
	}

	public Tag createLongList(LongStream longStream) {
		return new LongArrayTag(longStream.toArray());
	}

	public Tag createList(Stream<Tag> stream) {
		return ListTag.fromStream(stream);
	}

	public Tag remove(Tag tag, String string) {
		if (tag instanceof CompoundTag compoundTag) {
			CompoundTag compoundTag2 = compoundTag.shallowCopy();
			compoundTag2.remove(string);
			return compoundTag2;
		} else {
			return tag;
		}
	}

	public String toString() {
		return "NBT";
	}

	@Override
	public RecordBuilder<Tag> mapBuilder() {
		return new NbtRecordBuilder();
	}

	private static Optional<ListCollector> createCollector(Tag tag) {
		if (tag instanceof EndTag) {
			return Optional.of(new GenericListCollector());
		} else if (tag instanceof CollectionTag collectionTag) {
			if (collectionTag.isEmpty()) {
				return Optional.of(new GenericListCollector());
			} else {
				return switch (collectionTag) {
					case ListTag listTag -> Optional.of(new GenericListCollector(listTag));
					case ByteArrayTag byteArrayTag -> Optional.of(new ByteListCollector(byteArrayTag.getAsByteArray()));
					case IntArrayTag intArrayTag -> Optional.of(new IntListCollector(intArrayTag.getAsIntArray()));
					case LongArrayTag longArrayTag -> Optional.of(new LongListCollector(longArrayTag.getAsLongArray()));
					default -> throw new MatchException(null, null);
				};
			}
		} else {
			return Optional.empty();
		}
	}

	static class ByteListCollector implements ListCollector {
		private final ByteArrayList values = new ByteArrayList();

		public ByteListCollector(byte[] bs) {
			this.values.addElements(0, bs);
		}

		@Override
		public ListCollector accept(Tag tag) {
			if (tag instanceof ByteTag byteTag) {
				this.values.add(byteTag.byteValue());
				return this;
			} else {
				return new GenericListCollector(this.values).accept(tag);
			}
		}

		@Override
		public Tag result() {
			return new ByteArrayTag(this.values.toByteArray());
		}
	}

	static class GenericListCollector implements ListCollector {
		private final ListTag result = new ListTag();

		GenericListCollector() {
		}

		GenericListCollector(ListTag listTag) {
			this.result.addAll(listTag);
		}

		public GenericListCollector(IntArrayList intArrayList) {
			intArrayList.forEach(i -> this.result.add(IntTag.valueOf(i)));
		}

		public GenericListCollector(ByteArrayList byteArrayList) {
			byteArrayList.forEach(b -> this.result.add(ByteTag.valueOf(b)));
		}

		public GenericListCollector(LongArrayList longArrayList) {
			longArrayList.forEach(l -> this.result.add(LongTag.valueOf(l)));
		}

		@Override
		public ListCollector accept(Tag tag) {
			this.result.add(tag);
			return this;
		}

		@Override
		public Tag result() {
			return this.result;
		}
	}

	static class IntListCollector implements ListCollector {
		private final IntArrayList values = new IntArrayList();

		public IntListCollector(int[] is) {
			this.values.addElements(0, is);
		}

		@Override
		public ListCollector accept(Tag tag) {
			if (tag instanceof IntTag intTag) {
				this.values.add(intTag.intValue());
				return this;
			} else {
				return new GenericListCollector(this.values).accept(tag);
			}
		}

		@Override
		public Tag result() {
			return new IntArrayTag(this.values.toIntArray());
		}
	}

	interface ListCollector {
		ListCollector accept(Tag tag);

		default ListCollector acceptAll(Iterable<Tag> iterable) {
			ListCollector listCollector = this;

			for (Tag tag : iterable) {
				listCollector = listCollector.accept(tag);
			}

			return listCollector;
		}

		default ListCollector acceptAll(Stream<Tag> stream) {
			return this.acceptAll(stream::iterator);
		}

		Tag result();
	}

	static class LongListCollector implements ListCollector {
		private final LongArrayList values = new LongArrayList();

		public LongListCollector(long[] ls) {
			this.values.addElements(0, ls);
		}

		@Override
		public ListCollector accept(Tag tag) {
			if (tag instanceof LongTag longTag) {
				this.values.add(longTag.longValue());
				return this;
			} else {
				return new GenericListCollector(this.values).accept(tag);
			}
		}

		@Override
		public Tag result() {
			return new LongArrayTag(this.values.toLongArray());
		}
	}

	class NbtRecordBuilder extends AbstractStringBuilder<Tag, CompoundTag> {
		protected NbtRecordBuilder() {
			super(NbtOps.this);
		}

		protected CompoundTag initBuilder() {
			return new CompoundTag();
		}

		protected CompoundTag append(String string, Tag tag, CompoundTag compoundTag) {
			compoundTag.put(string, tag);
			return compoundTag;
		}

		protected DataResult<Tag> build(CompoundTag compoundTag, Tag tag) {
			if (tag == null || tag == EndTag.INSTANCE) {
				return DataResult.success(compoundTag);
			} else if (!(tag instanceof CompoundTag compoundTag2)) {
				return DataResult.error(() -> "mergeToMap called with not a map: " + tag, tag);
			} else {
				CompoundTag compoundTag3 = compoundTag2.shallowCopy();

				for (Entry<String, Tag> entry : compoundTag.entrySet()) {
					compoundTag3.put(entry.getKey(), entry.getValue());
				}

				return DataResult.success(compoundTag3);
			}
		}
	}
}
