package top.mcfpp.nbt.tags;

import top.mcfpp.nbt.visitors.TagVisitor;
import top.mcfpp.nbt.tags.collection.ListTag;

import java.util.Optional;

public interface Tag{

	String toString();

	Tag copy();

	void accept(TagVisitor tagVisitor);

	default Optional<String> asString() {
		return Optional.empty();
	}

	default Optional<Number> asNumber() {
		return Optional.empty();
	}

	default Optional<Byte> asByte() {
		return this.asNumber().map(Number::byteValue);
	}

	default Optional<Short> asShort() {
		return this.asNumber().map(Number::shortValue);
	}

	default Optional<Integer> asInt() {
		return this.asNumber().map(Number::intValue);
	}

	default Optional<Long> asLong() {
		return this.asNumber().map(Number::longValue);
	}

	default Optional<Float> asFloat() {
		return this.asNumber().map(Number::floatValue);
	}

	default Optional<Double> asDouble() {
		return this.asNumber().map(Number::doubleValue);
	}

	default Optional<Boolean> asBoolean() {
		return this.asByte().map(byte_ -> byte_ != 0);
	}

	default Optional<byte[]> asByteArray() {
		return Optional.empty();
	}

	default Optional<int[]> asIntArray() {
		return Optional.empty();
	}

	default Optional<long[]> asLongArray() {
		return Optional.empty();
	}

	default Optional<CompoundTag> asCompound() {
		return Optional.empty();
	}

	default Optional<ListTag> asList() {
		return Optional.empty();
	}
}
