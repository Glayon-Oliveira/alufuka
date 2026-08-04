package com.dev.top;

import static java.util.Calendar.JANUARY;

import java.io.Serializable;
import java.util.*;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * ...
 */
@Deprecated
@SuppressWarnings("unused")
public abstract class Test<T extends Number & Comparable<T>>
		extends BaseTest
		implements Serializable, Comparable<Test<T>> {

	private static final long serialVersionUID = 1L;

	private final T value;
	protected String name;
	public static int counter;
	volatile boolean active;
	transient Object cache;

	{
		active = true;
	}

	static {
		counter = 0;
	}

	public Test(T value, String name) {
		this.value = value;
		this.name = name;
	}

	protected Test(T value) throws IllegalArgumentException {
		this(value, "default");
	}

	/**
	 * ...
	 *
	 * @param ...
	 * @return ...
	 */
	public abstract T calculate(T value);

	protected final void validate(T value) {
		Objects.requireNonNull(value);
	}
	
	public static <E extends Number> List<E> convert(E value, List<? extends E> values) {
	    return List.of(value);
	}
	
	public static <E extends Number> List<E> convert(E value, List<? extends E> values, String... names) throws IllegalArgumentException {
		return List.of(value);
	}
	
	public static int compare(String[] vs1, String[] vs2) {
	    return Arrays.equals(vs1, vs2) ? 0 : 1;
	}

	public static int compare(String vs1, String vs2) {
	    return vs1.compareTo(vs2);
	}

	@Override
	public int compareTo(Test<T> other) {
		return value.compareTo(other.value);
	}

	public T getValue() {
		return value;
	}

	public String getName() {
		return name;
	}
	
	public String getName(String prefix) {
	    return prefix + name;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof Test<?> other)) {
			return false;
		}

		return Objects.equals(value, other.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(value);
	}

	public enum Status {
		ACTIVE,
		INACTIVE,
		UNKNOWN
	}

	public record Result<T>(T value, boolean success) {

		public Result {
			Objects.requireNonNull(value);
		}

		public static <E> Result<E> success(E value) {
			return new Result<>(value, true);
		}
		
		public static <E> Result<E> success(E value, boolean success) {
		    return new Result<>(value, success);
		}
	}

	public interface Factory<E> {

		E create();

		default E createDefault() {
			return create();
		}
	}

	protected static class NestedClass {

		private int value;
		
		public NestedClass() {}
		
		public NestedClass(int value) {
	        this.value = value;
	    }

		public int getValue() {
			return value;
		}
	}

	private static final class PrivateNestedClass {

	}

	@Override
	public String toString() {
		return "Test{" +
				"value=" + value +
				", name='" + name + '\'' +
				'}';
	}
}