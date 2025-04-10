package top.mcfpp.nbt.tags.collection;

import org.apache.commons.lang3.ArrayUtils;
import top.mcfpp.nbt.tags.Tag;
import top.mcfpp.nbt.tags.TagType;
import top.mcfpp.nbt.tags.primitive.ByteTag;
import top.mcfpp.nbt.tags.primitive.NumericTag;
import top.mcfpp.nbt.visitors.StringTagVisitor;
import top.mcfpp.nbt.visitors.TagVisitor;

import java.util.Arrays;
import java.util.Optional;

public final class ByteArrayTag implements CollectionTag {
	private static final int SELF_SIZE_IN_BYTES = 24;
	public static final TagType<ByteArrayTag> TYPE = new TagType.VariableSize<ByteArrayTag>() {

		@Override
		public String getName() {
			return "BYTE[]";
		}

		@Override
		public String getPrettyName() {
			return "TAG_Byte_Array";
		}
	};
	private byte[] data;

	public ByteArrayTag(byte[] bs) {
		this.data = bs;
	}

	@Override
	public int sizeInBytes() {
		return SELF_SIZE_IN_BYTES + this.data.length;
	}

	@Override
	public byte getId() {
		return TAG_BYTE_ARRAY;
	}

	@Override
	public TagType<ByteArrayTag> getType() {
		return TYPE;
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
	public boolean setTag(int i, Tag tag) {
		if (tag instanceof NumericTag numericTag) {
			this.data[i] = numericTag.byteValue();
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean addTag(int i, Tag tag) {
		if (tag instanceof NumericTag numericTag) {
			this.data = ArrayUtils.add(this.data, i, numericTag.byteValue());
			return true;
		} else {
			return false;
		}
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
