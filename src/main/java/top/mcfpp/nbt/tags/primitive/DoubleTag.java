package top.mcfpp.nbt.tags.primitive;

import top.mcfpp.nbt.tags.TagType;
import top.mcfpp.nbt.visitors.StringTagVisitor;
import top.mcfpp.nbt.visitors.TagVisitor;

public record DoubleTag(double value) implements NumericTag {
	private static final int SELF_SIZE_IN_BYTES = 16;
	public static final DoubleTag ZERO = new DoubleTag(0.0);
	public static final TagType<DoubleTag> TYPE = new TagType.StaticSize<DoubleTag>() {

		@Override
		public int size() {
			return 8;
		}

		@Override
		public String getName() {
			return "DOUBLE";
		}

		@Override
		public String getPrettyName() {
			return "TAG_Double";
		}
	};

    @Deprecated(
            forRemoval = true
    )
    public DoubleTag {
    }

	public static DoubleTag valueOf(double d) {
		return d == 0.0 ? ZERO : new DoubleTag(d);
	}

	@Override
	public int sizeInBytes() {
		return SELF_SIZE_IN_BYTES;
	}

	@Override
	public byte getId() {
		return TAG_DOUBLE;
	}

	@Override
	public TagType<DoubleTag> getType() {
		return TYPE;
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
