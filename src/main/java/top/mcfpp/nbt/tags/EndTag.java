package top.mcfpp.nbt.tags;

import top.mcfpp.nbt.visitors.StringTagVisitor;
import top.mcfpp.nbt.visitors.TagVisitor;

public final class EndTag implements Tag {
	private static final int SELF_SIZE_IN_BYTES = 8;
	public static final TagType<EndTag> TYPE = new TagType<EndTag>() {

		@Override
		public String getName() {
			return "END";
		}

		@Override
		public String getPrettyName() {
			return "TAG_End";
		}
	};
	public static final EndTag INSTANCE = new EndTag();

	private EndTag() {
	}

	@Override
	public int sizeInBytes() {
		return SELF_SIZE_IN_BYTES;
	}

	@Override
	public byte getId() {
		return TAG_END;
	}

	@Override
	public TagType<EndTag> getType() {
		return TYPE;
	}

	@Override
	public String toString() {
		StringTagVisitor stringTagVisitor = new StringTagVisitor();
		stringTagVisitor.visitEnd(this);
		return stringTagVisitor.build();
	}

	public EndTag copy() {
		return this;
	}

	@Override
	public void accept(TagVisitor tagVisitor) {
		tagVisitor.visitEnd(this);
	}
}
