package top.mcfpp.nbt.tags.collection;

import org.apache.commons.lang3.ArrayUtils;
import top.mcfpp.nbt.tags.Tag;
import top.mcfpp.nbt.tags.primitive.ByteTag;
import top.mcfpp.nbt.tags.primitive.NumericTag;
import top.mcfpp.nbt.visitors.StringTagVisitor;
import top.mcfpp.nbt.visitors.TagVisitor;

import java.util.Arrays;
import java.util.Optional;

public final class ByteArrayTag implements CollectionTag<ByteTag> {
	private byte[] data;

	public ByteArrayTag(byte[] bs) {
		this.data = bs;
	}

	@Override
	public String toString() {
		StringTagVisitor stringTagVisitor = new StringTagVisitor();
		stringTagVisitor.visitByteArray(this);
		return stringTagVisitor.build();
	}

	@Override
	public Tag copy() {
		byte[] bs = new byte[this.data.length];
		System.arraycopy(this.data, 0, bs, 0, this.data.length);
		return new ByteArrayTag(bs);
	}

	public boolean equals(Object object) {
		return this == object || object instanceof ByteArrayTag && Arrays.equals(this.data, ((ByteArrayTag) object).data);
	}

	public int hashCode() {
		return Arrays.hashCode(this.data);
	}

	@Override
	public void accept(TagVisitor tagVisitor) {
		tagVisitor.visitByteArray(this);
	}

	public byte[] getAsByteArray() {
		return this.data;
	}

	@Override
	public int size() {
		return this.data.length;
	}

	public ByteTag get(int i) {
		return ByteTag.valueOf(this.data[i]);
	}

	@Override
	public ByteTag set(int i, ByteTag tag) {
		byte b = this.data[i];
		this.data[i] = tag.byteValue();
		return ByteTag.valueOf(b);
	}

	@Override
	public void add(int i, ByteTag tag) {
		this.data = ArrayUtils.insert(i, this.data, tag.byteValue());
	}

	public ByteTag remove(int i) {
		byte b = this.data[i];
		this.data = ArrayUtils.remove(this.data, i);
		return ByteTag.valueOf(b);
	}

	@Override
	public void clear() {
		this.data = new byte[0];
	}

	@Override
	public Optional<byte[]> asByteArray() {
		return Optional.of(this.data);
	}

}
