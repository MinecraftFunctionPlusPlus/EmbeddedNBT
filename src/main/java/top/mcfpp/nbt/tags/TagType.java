package top.mcfpp.nbt.tags;

import java.io.IOException;

public interface TagType<T extends Tag> {

	String getName();

	String getPrettyName();

	static TagType<EndTag> createInvalid(int i) {
		return new TagType<EndTag>() {
			private IOException createException() {
				return new IOException("Invalid tag id: " + i);
			}

			@Override
			public String getName() {
				return "INVALID[" + i + "]";
			}

			@Override
			public String getPrettyName() {
				return "UNKNOWN_" + i;
			}
		};
	}

	interface StaticSize<T extends Tag> extends TagType<T> {

		int size();
	}

	interface VariableSize<T extends Tag> extends TagType<T> {

	}
}
