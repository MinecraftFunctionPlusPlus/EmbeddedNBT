package top.mcfpp.nbt.tags.primitive;

import top.mcfpp.nbt.tags.Tag;

public sealed interface PrimitiveTag extends Tag permits NumericTag, StringTag {
	@Override
	default Tag copy() {
		return this;
	}
}
