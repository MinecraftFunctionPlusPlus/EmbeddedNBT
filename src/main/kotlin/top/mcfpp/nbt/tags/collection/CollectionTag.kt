package top.mcfpp.nbt.tags.collection;

import org.jetbrains.annotations.NotNull;
import top.mcfpp.nbt.tags.Tag;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public sealed interface CollectionTag<T extends Tag> extends Iterable<T>, Tag permits ListTag, ByteArrayTag, IntArrayTag, LongArrayTag {
	void clear();

	T set(int i, T tag);

	void add(int i, T tag);

	T remove(int i);

	T get(int i);

	int size();

	default boolean isEmpty() {
		return this.size() == 0;
	}

	@NotNull
	default Iterator<T> iterator() {
		return new Iterator<>() {
			private int index;

			public boolean hasNext() {
				return this.index < CollectionTag.this.size();
			}

			public T next() {
				if (!this.hasNext()) {
					throw new NoSuchElementException();
				} else {
					return CollectionTag.this.get(this.index++);
				}
			}
		};
	}

	default Stream<T> stream() {
		return StreamSupport.stream(this.spliterator(), false);
	}
}
