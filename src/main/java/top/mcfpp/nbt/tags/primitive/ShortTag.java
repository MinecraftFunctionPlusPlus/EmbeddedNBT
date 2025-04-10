package top.mcfpp.nbt.tags.primitive;

import top.mcfpp.nbt.tags.TagType;
import top.mcfpp.nbt.visitors.StringTagVisitor;
import top.mcfpp.nbt.visitors.TagVisitor;

public record  ShortTag(short value) implements NumericTag {
	private static final int SELF_SIZE_IN_BYTES = 10;
	public static final TagType<ShortTag> TYPE = new TagType.StaticSize<ShortTag>() {

		@Override
		public int size() {
			return 2;
		}

		@Override
		public String getName() {
			return "SHORT";
		}

		@Override
		public String getPrettyName() {
			return "TAG_Short";
		}
	};

    @Deprecated(
            forRemoval = true
    )
    public ShortTag {
    }

	public static ShortTag valueOf(short s) {
		return s >= Cache.LOW && s <= Cache.HIGH ? Cache.cache[s - Cache.LOW] : new ShortTag(s);
	}

	@Override
	public int sizeInBytes() {
		return SELF_SIZE_IN_BYTES;
	}

	@Override
	public byte getId() {
		return TAG_SHORT;
	}

	@Override
	public TagType<ShortTag> getType() {
		return TYPE;
	}

	public ShortTag copy() {
		return this;
	}

	@Override
	public void accept(TagVisitor tagVisitor) {
		tagVisitor.visitShort(this);
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
		return (byte)(this.value & 255);
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
		stringTagVisitor.visitShort(this);
		return stringTagVisitor.build();
	}

	static class Cache {
		private static final int HIGH = 1024;
		private static final int LOW = -128;
		static final ShortTag[] cache = new ShortTag[1153];

		private Cache() {
		}

		static {
			for (int i = 0; i < cache.length; i++) {
				cache[i] = new ShortTag((short)(LOW + i));
			}
		}
	}
}
