package top.mcfpp.nbt.tags.collection;

import org.apache.commons.lang3.ArrayUtils;
import top.mcfpp.nbt.tags.Tag;
import top.mcfpp.nbt.tags.TagType;
import top.mcfpp.nbt.tags.primitive.IntTag;
import top.mcfpp.nbt.tags.primitive.NumericTag;
import top.mcfpp.nbt.visitors.StringTagVisitor;
import top.mcfpp.nbt.visitors.TagVisitor;

import java.util.Arrays;
import java.util.Optional;

public final class IntArrayTag implements CollectionTag {
	private static final int SELF_SIZE_IN_BYTES = 24;
	public static final TagType<IntArrayTag> TYPE = new TagType.VariableSize<IntArrayTag>() {

		@Override
		public String getName() {
			return "INT[]";
		}

		@Override
		public String getPrettyName() {
			return "TAG_Int_Array";
		}
	};
	private int[] data;

	public IntArrayTag(int[] is) {
		this.data = is;
	}

	@Override
	public int sizeInBytes() {
		return SELF_SIZE_IN_BYTES + 4 * this.data.length;
	}

	@Override
	public byte getId() {
		return TAG_INT_ARRAY;
	}

	@Override
	public TagType<IntArrayTag> getType() {
		return TYPE;
	}

	@Override
	public String toString() {
		StringTagVisitor stringTagVisitor = new StringTagVisitor();
		stringTagVisitor.visitIntArray(this);
		return stringTagVisitor.build();
	}

	public IntArrayTag copy() {
		int[] is = new int[this.data.length];
		System.arraycopy(this.data, 0, is, 0, this.data.length);
		return new IntArrayTag(is);
	}

	public boolean equals(Object object) {
		return this == object || object instanceof IntArrayTag && Arrays.equals(this.data, ((IntArrayTag) object).data);
	}

	public int hashCode() {
		return Arrays.hashCode(this.data);
	}

	public int[] getAsIntArray() {
		return this.data;
	}

	@Override
	public void accept(TagVisitor tagVisitor) {
		tagVisitor.visitIntArray(this);
	}

	@Override
	public int size() {
		return this.data.length;
	}

	public IntTag get(int i) {
		return IntTag.valueOf(this.data[i]);
	}

	@Override
	public boolean setTag(int i, Tag tag) {
		if (tag instanceof NumericTag numericTag) {
			this.data[i] = numericTag.intValue();
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean addTag(int i, Tag tag) {
		if (tag instanceof NumericTag numericTag) {
			this.data = ArrayUtils.add(this.data, i, numericTag.intValue());
			return true;
		} else {
			return false;
		}
	}

	public IntTag remove(int i) {
		int j = this.data[i];
		this.data = ArrayUtils.remove(this.data, i);
		return IntTag.valueOf(j);
	}

	@Override
	public void clear() {
		this.data = new int[0];
	}

	@Override
	public Optional<int[]> asIntArray() {
		return Optional.of(this.data);
	}

}
