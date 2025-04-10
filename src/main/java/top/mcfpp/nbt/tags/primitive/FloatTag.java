package top.mcfpp.nbt.tags.primitive;

import top.mcfpp.nbt.tags.TagType;
import top.mcfpp.nbt.visitors.StringTagVisitor;
import top.mcfpp.nbt.visitors.TagVisitor;

public record FloatTag(float value) implements NumericTag {
	private static final int SELF_SIZE_IN_BYTES = 12;
	public static final FloatTag ZERO = new FloatTag(0.0F);
	public static final TagType<FloatTag> TYPE = new TagType.StaticSize<FloatTag>() {

		@Override
		public int size() {
			return 4;
		}

		@Override
		public String getName() {
			return "FLOAT";
		}

		@Override
		public String getPrettyName() {
			return "TAG_Float";
		}
	};

    @Deprecated(forRemoval = true)
    public FloatTag {}

	public static FloatTag valueOf(float f) {
		return f == 0.0F ? ZERO : new FloatTag(f);
	}

	@Override
	public int sizeInBytes() {
		return SELF_SIZE_IN_BYTES;
	}

	@Override
	public byte getId() {
		return TAG_FLOAT;
	}

	@Override
	public TagType<FloatTag> getType() {
		return TYPE;
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
