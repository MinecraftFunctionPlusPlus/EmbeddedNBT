package top.mcfpp.nbt.tags.primitive;

import top.mcfpp.nbt.tags.TagType;
import top.mcfpp.nbt.visitors.StringTagVisitor;
import top.mcfpp.nbt.visitors.TagVisitor;

public record LongTag(long value) implements NumericTag {
	private static final int SELF_SIZE_IN_BYTES = 16;
	public static final TagType<LongTag> TYPE = new TagType.StaticSize<LongTag>() {

		@Override
		public int size() {
			return 8;
		}

		@Override
		public String getName() {
			return "LONG";
		}

		@Override
		public String getPrettyName() {
			return "TAG_Long";
		}
	};

    @Deprecated(forRemoval = true)
    public LongTag {
    }

	public static LongTag valueOf(long l) {
		return l >= Cache.LOW && l <= Cache.HIGH ? Cache.cache[(int)l - Cache.LOW] : new LongTag(l);
	}

	@Override
	public int sizeInBytes() {
		return SELF_SIZE_IN_BYTES;
	}

	@Override
	public byte getId() {
		return TAG_LONG;
	}

	@Override
	public TagType<LongTag> getType() {
		return TYPE;
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
