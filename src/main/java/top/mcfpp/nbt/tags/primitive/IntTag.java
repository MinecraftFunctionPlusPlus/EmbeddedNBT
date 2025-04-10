package top.mcfpp.nbt.tags.primitive;

import top.mcfpp.nbt.tags.TagType;
import top.mcfpp.nbt.visitors.StringTagVisitor;
import top.mcfpp.nbt.visitors.TagVisitor;

public record IntTag(int value) implements NumericTag {
	private static final int SELF_SIZE_IN_BYTES = 12;
	public static final TagType<IntTag> TYPE = new TagType.StaticSize<IntTag>() {

		@Override
		public int size() {
			return 4;
		}

		@Override
		public String getName() {
			return "INT";
		}

		@Override
		public String getPrettyName() {
			return "TAG_Int";
		}
	};

    @Deprecated(
            forRemoval = true
    )
    public IntTag {
    }

	public static IntTag valueOf(int i) {
		return i >= Cache.LOW && i <= Cache.HIGH ? Cache.cache[i - Cache.LOW] : new IntTag(i);
	}

	@Override
	public int sizeInBytes() {
		return SELF_SIZE_IN_BYTES;
	}

	@Override
	public byte getId() {
		return TAG_INT;
	}

	@Override
	public TagType<IntTag> getType() {
		return TYPE;
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
