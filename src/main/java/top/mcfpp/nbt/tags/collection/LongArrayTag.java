package top.mcfpp.nbt.tags.collection;

import org.apache.commons.lang3.ArrayUtils;
import top.mcfpp.nbt.tags.Tag;
import top.mcfpp.nbt.tags.TagType;
import top.mcfpp.nbt.tags.primitive.LongTag;
import top.mcfpp.nbt.tags.primitive.NumericTag;
import top.mcfpp.nbt.visitors.StringTagVisitor;
import top.mcfpp.nbt.visitors.TagVisitor;

import java.util.Arrays;
import java.util.Optional;

public final class LongArrayTag implements CollectionTag {
	private static final int SELF_SIZE_IN_BYTES = 24;
	public static final TagType<LongArrayTag> TYPE = new TagType.VariableSize<LongArrayTag>() {

		@Override
		public String getName() {
			return "LONG[]";
		}

		@Override
		public String getPrettyName() {
			return "TAG_Long_Array";
		}
	};
	private long[] data;

	public LongArrayTag(long[] ls) {
		this.data = ls;
	}


	@Override
	public int sizeInBytes() {
		return SELF_SIZE_IN_BYTES + 8 * this.data.length;
	}

	@Override
	public byte getId() {
		return TAG_LONG_ARRAY;
	}

	@Override
	public TagType<LongArrayTag> getType() {
		return TYPE;
	}

	@Override
	public String toString() {
		StringTagVisitor stringTagVisitor = new StringTagVisitor();
		stringTagVisitor.visitLongArray(this);
		return stringTagVisitor.build();
	}

	public LongArrayTag copy() {
		long[] ls = new long[this.data.length];
		System.arraycopy(this.data, 0, ls, 0, this.data.length);
		return new LongArrayTag(ls);
	}

	public boolean equals(Object object) {
		return this == object || object instanceof LongArrayTag && Arrays.equals(this.data, ((LongArrayTag) object).data);
	}

	public int hashCode() {
		return Arrays.hashCode(this.data);
	}

	@Override
	public void accept(TagVisitor tagVisitor) {
		tagVisitor.visitLongArray(this);
	}

	public long[] getAsLongArray() {
		return this.data;
	}

	@Override
	public int size() {
		return this.data.length;
	}

	public LongTag get(int i) {
		return LongTag.valueOf(this.data[i]);
	}

	@Override
	public boolean setTag(int i, Tag tag) {
		if (tag instanceof NumericTag numericTag) {
			this.data[i] = numericTag.longValue();
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean addTag(int i, Tag tag) {
		if (tag instanceof NumericTag numericTag) {
			this.data = ArrayUtils.add(this.data, i, numericTag.longValue());
			return true;
		} else {
			return false;
		}
	}

	public LongTag remove(int i) {
		long l = this.data[i];
		this.data = ArrayUtils.remove(this.data, i);
		return LongTag.valueOf(l);
	}

	@Override
	public void clear() {
		this.data = new long[0];
	}

	@Override
	public Optional<long[]> asLongArray() {
		return Optional.of(this.data);
	}


}
