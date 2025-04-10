package top.mcfpp.nbt.tags.primitive;

import top.mcfpp.nbt.visitors.StringTagVisitor;
import top.mcfpp.nbt.visitors.TagVisitor;

public record IntTag(int value) implements NumericTag {
	public static IntTag valueOf(int i) {
		return i >= Cache.LOW && i <= Cache.HIGH ? Cache.cache[i - Cache.LOW] : new IntTag(i);
	}

	public IntTag copy() {
		return this;
	}

	@Override
	public void accept(TagVisitor tagVisitor) {
		tagVisitor.visitInt(this);
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
		return (short)(this.value & 65535);
	}

	@Override
	public byte byteValue() {
		return (byte)(this.value & 0xFF);
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
		stringTagVisitor.visitInt(this);
		return stringTagVisitor.build();
	}

	static class Cache {
		private static final int HIGH = 1024;
		private static final int LOW = -128;
		static final IntTag[] cache = new IntTag[1153];

		private Cache() {
		}

		static {
			for (int i = 0; i < cache.length; i++) {
				cache[i] = new IntTag(LOW + i);
			}
		}
	}
}
