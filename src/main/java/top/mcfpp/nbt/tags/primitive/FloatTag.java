package top.mcfpp.nbt.tags.primitive;

import top.mcfpp.nbt.visitors.StringTagVisitor;
import top.mcfpp.nbt.visitors.TagVisitor;

public record FloatTag(float value) implements NumericTag {
	public static final FloatTag ZERO = new FloatTag(0.0F);
	public static FloatTag valueOf(float f) {
		return f == 0.0F ? ZERO : new FloatTag(f);
	}

	public FloatTag copy() {
		return this;
	}

	@Override
	public void accept(TagVisitor tagVisitor) {
		tagVisitor.visitFloat(this);
	}

	@Override
	public long longValue() {
		return (long)this.value;
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
		return this.value;
	}

	@Override
	public Number box() {
		return this.value;
	}


	@Override
	public String toString() {
		StringTagVisitor stringTagVisitor = new StringTagVisitor();
		stringTagVisitor.visitFloat(this);
		return stringTagVisitor.build();
	}
}
