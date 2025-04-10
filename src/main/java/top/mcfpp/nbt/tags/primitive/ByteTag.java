package top.mcfpp.nbt.tags.primitive;

import top.mcfpp.nbt.visitors.StringTagVisitor;
import top.mcfpp.nbt.visitors.TagVisitor;

public record ByteTag(byte value) implements NumericTag {
	public static final ByteTag ZERO = valueOf((byte)0);
	public static final ByteTag ONE = valueOf((byte)1);

	public static ByteTag valueOf(byte b) {
		return Cache.cache[128 + b];
	}

	public static ByteTag valueOf(boolean bl) {
		return bl ? ONE : ZERO;
	}

	public ByteTag copy() {
		return this;
	}

	@Override
	public void accept(TagVisitor tagVisitor) {
		tagVisitor.visitByte(this);
	}

	@Override
	public long longValue() {
		return this.value;
	}

	@Override
	public int intValue() {
		return this.value;
	}

	@Override
	public short shortValue() {
		return this.value;
	}

	@Override
	public byte byteValue() {
		return this.value;
	}

	@Override
	public double doubleValue() {
		return this.value;
	}

	@Override
	public float floatValue() {
		return this.value;
	}

	@Override
	public Number box() {
		return this.value;
	}

	@Override
	public String toString() {
		StringTagVisitor stringTagVisitor = new StringTagVisitor();
		stringTagVisitor.visitByte(this);
		return stringTagVisitor.build();
	}

	static class Cache {
		static final ByteTag[] cache = new ByteTag[256];

		private Cache() {
		}

		static {
			for (int i = 0; i < cache.length; i++) {
				cache[i] = new ByteTag((byte)(i - 128));
			}
		}
	}
}
