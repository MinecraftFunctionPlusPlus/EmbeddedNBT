package top.mcfpp.nbt.tags.collection;

import org.apache.commons.lang3.ArrayUtils;
import top.mcfpp.nbt.tags.Tag;
import top.mcfpp.nbt.tags.primitive.LongTag;
import top.mcfpp.nbt.tags.primitive.NumericTag;
import top.mcfpp.nbt.visitors.StringTagVisitor;
import top.mcfpp.nbt.visitors.TagVisitor;

import java.util.Arrays;
import java.util.Optional;

public final class LongArrayTag implements CollectionTag<LongTag> {
	private long[] data;

	public LongArrayTag(long[] ls) {
		this.data = ls;
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
	public LongTag set(int i, LongTag tag) {
		long l = this.data[i];
		this.data[i] = tag.longValue();
		return LongTag.valueOf(l);
	}

	@Override
	public void add(int i, LongTag tag) {
		this.data = ArrayUtils.insert(i, this.data, tag.longValue());
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
