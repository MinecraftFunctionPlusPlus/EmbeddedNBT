package top.mcfpp.nbt.tags.primitive;

import top.mcfpp.nbt.visitors.StringTagVisitor;
import top.mcfpp.nbt.visitors.TagVisitor;

public record DoubleTag(double value) implements NumericTag {
	public static final DoubleTag ZERO = new DoubleTag(0.0);
	public static DoubleTag valueOf(double d) {
		return d == 0.0 ? ZERO : new DoubleTag(d);
	}

	public DoubleTag copy() {
		return this;
	}

	@Override
	public void accept(TagVisitor tagVisitor) {
		tagVisitor.visitDouble(this);
	}

	@Override
	public long longValue() {
		return (long)Math.floor(this.value);
	}

	@Override
	public int intValue() {
		return top.mcfpp.utils.Math.floor(this.value);
	}

	@Override
	public short shortValue() {
		return (short)(top.mcfpp.utils.Math.floor(this.value) & 65535);
	}

	@Override
	public byte byteValue() {
		return (byte)(top.mcfpp.utils.Math.floor(this.value) & 0xFF);
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
		stringTagVisitor.visitDouble(this);
		return stringTagVisitor.build();
	}
}
