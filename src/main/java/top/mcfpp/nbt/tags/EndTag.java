package top.mcfpp.nbt.tags;

import top.mcfpp.nbt.visitors.StringTagVisitor;
import top.mcfpp.nbt.visitors.TagVisitor;

public final class EndTag implements Tag {
	public static final EndTag INSTANCE = new EndTag();

	private EndTag() {}

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
