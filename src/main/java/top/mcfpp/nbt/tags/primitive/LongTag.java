package top.mcfpp.nbt.tags.primitive;

import top.mcfpp.nbt.visitors.StringTagVisitor;
import top.mcfpp.nbt.visitors.TagVisitor;

public record LongTag(long value) implements NumericTag {
	public static LongTag valueOf(long l) {
		return l >= Cache.LOW && l <= Cache.HIGH ? Cache.cache[(int)l - Cache.LOW] : new LongTag(l);
	}

	public LongTag copy() {
		return this;
	}

	@Override
	public void accept(TagVisitor tagVisitor) {
		tagVisitor.visitLong(this);
	}

	@Override
	public long longValue() {
		return this.value;
	}

	@Override
	public int intValue() {
		return (int)(this.value & -1L);
	}

	@Override
	public short shortValue() {
		return (short)(this.value & 65535L);
	}

	@Override
	public byte byteValue() {
		return (byte)(this.value & 255L);
	}

	@Override
	public double doubleValue() {
		return this.value;
	}

	@Override
	public float floatValue() {
		return (float)this.value;
	}

	@Override
	public Number box() {
		return this.value;
	}

	@Override
	public String toString() {
		StringTagVisitor stringTagVisitor = new StringTagVisitor();
		stringTagVisitor.visitLong(this);
		return stringTagVisitor.build();
	}

	static class Cache {
		private static final int HIGH = 1024;
		private static final int LOW = -128;
		static final LongTag[] cache = new LongTag[1153];

		private Cache() {
		}

		static {
			for (int i = 0; i < cache.length; i++) {
				cache[i] = new LongTag(LOW + i);
			}
		}
	}
}
