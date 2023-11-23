declare namespace $tf {

	// interface java.util.function.BiConsumer
	interface BiConsumer<T, U> {
		accept(t: T, u: U): void;
		andThen(after: BiConsumer<T, U>): BiConsumer<T, U>;
	}

	// interface java.util.function.BiFunction
	interface BiFunction<T, U, R> {
		andThen<V>(after: Function<R, V>): BiFunction<T, U, V>;
		apply(t: T, u: U): R;
	}

	// interface java.util.function.BinaryOperator
	abstract class BinaryOperator<T> {
		static maxBy<T>(comparator: Comparator<T>): BinaryOperator<T>;
		static minBy<T>(comparator: Comparator<T>): BinaryOperator<T>;
	}
	interface BinaryOperator<T> extends BiFunction<T, T, T> {
	}

	// interface java.lang.CharSequence
	interface CharSequence {
		charAt(index: number): number;
		length(): number;
		subSequence(start: number, end: number): CharSequence;
		toString(): string;
	}

	// interface java.util.Collection
	interface Collection<E> extends Iterable<E> {
		add(o: E): boolean;
		addAll(c: Collection<E>): boolean;
		addAllJs(...args: E[]): boolean;
		clear(): void;
		contains(o: any): boolean;
		containsAll(c: Collection<any>): boolean;
		containsAllJs(...args: E[]): boolean;
		isEmpty(): boolean;
		remove(o: any): boolean;
		removeAll(c: Collection<any>): boolean;
		removeAllJs(...args: E[]): boolean;
		retainAll(c: Collection<any>): boolean;
		retainAllJs(...args: E[]): boolean;
		size(): number;
		stream(): Stream<E>;
		toArray(): any[];
	}

	// interface java.util.stream.Collector
	abstract class Collector<T, A, R> {
		static of<T, R>(supplier: Supplier<R>, accumulator: BiConsumer<R, T>, combiner: BinaryOperator<R>, ...characteristics: Collector$Characteristics[]): Collector<T, R, R>;
		static of2<T, A, R>(supplier: Supplier<A>, accumulator: BiConsumer<A, T>, combiner: BinaryOperator<A>, finisher: Function<A, R>, ...characteristics: Collector$Characteristics[]): Collector<T, A, R>;
	}
	interface Collector<T, A, R> {
		accumulator(): BiConsumer<A, T>;
		characteristics(): Set<Collector$Characteristics>;
		combiner(): BinaryOperator<A>;
		finisher(): Function<A, R>;
		supplier(): Supplier<A>;
	}

	// interface java.lang.Comparable
	interface Comparable<T> {
		compareTo(o: T): number;
	}

	// interface java.util.Comparator
	abstract class Comparator<T> {
		static comparing<T, U extends Comparable<U>>(keyExtractor: Function<T, U>): Comparator<T>;
		static comparingWith<T, U>(keyExtractor: Function<T, U>, keyComparator: Comparator<U>): Comparator<T>;
		static naturalOrder<T extends Comparable<T>>(): Comparator<T>;
		static nullsFirst<T>(comparator: Comparator<T>): Comparator<T>;
		static nullsLast<T>(comparator: Comparator<T>): Comparator<T>;
		static reverseOrder<T extends Comparable<T>>(): Comparator<T>;
	}
	interface Comparator<T> {
		compare(a: T, b: T): number;
		equals(other: any): boolean;
		reversed(): Comparator<T>;
		thenComparing(other: Comparator<T>): Comparator<T>;
		thenComparingBy<U extends Comparable<U>>(keyExtractor: Function<T, U>): Comparator<T>;
		thenComparingByWith<U>(keyExtractor: Function<T, U>, keyComparator: Comparator<U>): Comparator<T>;
	}

	// interface java.util.function.Consumer
	interface Consumer<T> {
		accept(t: T): void;
		andThen(after: Consumer<T>): Consumer<T>;
	}

	// interface java.util.function.Function
	abstract class Function<T, R> {
		static identity<T>(): Function<T, T>;
	}
	interface Function<T, R> {
		andThenFunction<V>(after: Function<R, V>): Function<T, V>;
		apply(t: T): R;
		compose<V>(before: Function<V, T>): Function<V, R>;
	}

	// interface java.lang.Iterable
	interface Iterable<T> {
		each(action: Consumer<T>): void;
		forEach(consumer: (t: T) => void): void;
		iterable(): globalThis.Iterable<T>;
		iterator(): Iterator<T>;
	}

	// interface java.util.Iterator
	interface Iterator<E> {
		forEachRemaining(consumer: Consumer<E>): void;
		hasNext(): boolean;
		next(): E;
		remove(): void;
	}

	// interface java.util.List
	interface List<E> extends Collection<E> {
		addAllAtIndex(index: number, c: Collection<E>): boolean;
		addAtIndex(index: number, element: E): void;
		getAtIndex(index: number): E;
		indexOf(o: any): number;
		lastIndexOf(o: any): number;
		removeAtIndex(index: number): E;
		setAtIndex(index: number, element: E): E;
		sort(c: Comparator<E>): void;
		subList(fromIndex: number, toIndex: number): List<E>;
	}

	// interface java.util.ListIterator
	interface ListIterator<E> extends Iterator<E> {
		add(o: E): void;
		hasPrevious(): boolean;
		nextIndex(): number;
		previous(): E;
		previousIndex(): number;
		remove(): void;
		set(o: E): void;
	}

	// interface java.util.Map
	interface Map<K, V> {
		clear(): void;
		containsKey(key: any): boolean;
		containsValue(value: any): boolean;
		entrySet(): Set<Map$Entry<K, V>>;
		forEach(consumer: (t: K, u: V) => void): void;
		get(key: any): V;
		getOrDefault(key: any, defaultValue: V): V;
		isEmpty(): boolean;
		keySet(): Set<K>;
		put(key: K, value: V): V;
		putAll(t: Map<K, V>): void;
		putIfAbsent(key: K, value: V): V;
		remove(key: any): V;
		replace(key: K, value: V): V;
		size(): number;
		toJsMap(): globalThis.Map<K, V>;
		values(): Collection<V>;
	}

	// interface java.util.Map$Entry
	interface Map$Entry<K, V> {
		equals(o: any): boolean;
		getKey(): K;
		getValue(): V;
		hashCode(): number;
		setValue(value: V): V;
	}

	// interface java.util.function.Predicate
	abstract class Predicate<T> {
		static isEqual<T>(targetRef: any): Predicate<T>;
	}
	interface Predicate<T> {
		and(other: Predicate<T>): Predicate<T>;
		negate(): Predicate<T>;
		or(other: Predicate<T>): Predicate<T>;
		test(t: T): boolean;
	}

	// interface java.util.Set
	interface Set<E> extends Collection<E> {
		toJsSet(): globalThis.Set<E>;
	}

	// interface java.util.stream.Stream
	abstract class Stream<T> {
		static concat<T>(a: Stream<T>, b: Stream<T>): Stream<T>;
		static empty<T>(): Stream<T>;
		static generate<T>(s: Supplier<T>): Stream<T>;
		static iterate<T>(seed: T, f: UnaryOperator<T>): Stream<T>;
		static of<T>(t: T): Stream<T>;
		static ofArray<T>(...values: T[]): Stream<T>;
	}
	interface Stream<T> {
		allMatch(predicate: Predicate<T>): boolean;
		anyMatch(predicate: Predicate<T>): boolean;
		collect<R, A>(collector: Collector<T, A, R>): R;
		collectWithCombiner<R>(supplier: Supplier<R>, accumulator: BiConsumer<R, T>, combiner: BiConsumer<R, R>): R;
		count(): Long;
		distinct(): Stream<T>;
		filter(predicate: Predicate<T>): Stream<T>;
		filterJs(predicate: (t: T) => boolean): Stream<T>;
		findAny(): Optional<T>;
		findFirst(): Optional<T>;
		flatMap<R>(mapper: Function<T, Stream<R>>): Stream<R>;
		flatMapJs<R>(mapper: (t: T) => Stream<R>): Stream<R>;
		forEach(action: Consumer<T>): void;
		forEachJs(action: (t: T) => void): void;
		forEachOrdered(action: Consumer<T>): void;
		forEachOrderedJs(action: Consumer<T>): void;
		iterable(): globalThis.Iterable<T>;
		limit(maxSize: Long): Stream<T>;
		map<R>(mapper: Function<T, R>): Stream<R>;
		mapJs<R>(mapper: (t: T) => R): Stream<R>;
		max(comparator: Comparator<T>): Optional<T>;
		min(comparator: Comparator<T>): Optional<T>;
		noneMatch(predicate: Predicate<T>): boolean;
		peek(action: Consumer<T>): Stream<T>;
		reduce(accumulator: BinaryOperator<T>): Optional<T>;
		reduceWithIdentity(identity: T, accumulator: BinaryOperator<T>): T;
		reduceWithIdentityAndCombiner<U>(identity: U, accumulator: BiFunction<U, T, U>, combiner: BinaryOperator<U>): U;
		skip(n: Long): Stream<T>;
		sorted(): Stream<T>;
		sortedWithComparator(comparator: Comparator<T>): Stream<T>;
		toArray(): any[];
	}

	// interface java.util.function.Supplier
	interface Supplier<T> {
		get(): T;
	}

	// interface java.util.function.UnaryOperator
	abstract class UnaryOperator<T> {
		static identity<T>(): UnaryOperator<T>;
	}
	interface UnaryOperator<T> extends Function<T, T> {
	}

	// enum java.util.stream.Collector$Characteristics
	interface Collector$Characteristics extends Comparable<Collector$Characteristics>{}
	class Collector$Characteristics {
		static CONCURRENT: Collector$Characteristics;
		static IDENTITY_FINISH: Collector$Characteristics;
		static UNORDERED: Collector$Characteristics;
	}

	// class java.math.BigDecimal
	interface BigDecimal extends Comparable<BigDecimal> {}
	class BigDecimal {
		static ONE: BigDecimal;
		static ROUND_CEILING: number;
		static ROUND_DOWN: number;
		static ROUND_FLOOR: number;
		static ROUND_HALF_DOWN: number;
		static ROUND_HALF_EVEN: number;
		static ROUND_HALF_UP: number;
		static ROUND_UNNECESSARY: number;
		static ROUND_UP: number;
		static TEN: BigDecimal;
		static ZERO: BigDecimal;
		static valueOfDouble(val: number): BigDecimal;
		static valueOfLong(unscaledVal: Long): BigDecimal;
		static valueOfLongWithScale(unscaledVal: Long, scale: number): BigDecimal;
		abs(): BigDecimal;
		add(augend: BigDecimal): BigDecimal;
		byteValueExact(): number;
		compareTo(val: BigDecimal): number;
		divide(divisor: BigDecimal): BigDecimal;
		divideAndRemainder(divisor: BigDecimal): BigDecimal[];
		divideToIntegralValue(divisor: BigDecimal): BigDecimal;
		divideWithRoundingMode(divisor: BigDecimal, roundingMode: number): BigDecimal;
		divideWithScaleAndRoundingMode(divisor: BigDecimal, scale: number, roundingMode: number): BigDecimal;
		doubleValue(): number;
		equals(x: any): boolean;
		floatValue(): number;
		hashCode(): number;
		intValue(): number;
		intValueExact(): number;
		longValue(): Long;
		longValueExact(): Long;
		max(val: BigDecimal): BigDecimal;
		min(val: BigDecimal): BigDecimal;
		movePointLeft(n: number): BigDecimal;
		movePointRight(n: number): BigDecimal;
		multiply(multiplicand: BigDecimal): BigDecimal;
		negate(): BigDecimal;
		plus(): BigDecimal;
		pow(n: number): BigDecimal;
		precision(): number;
		remainder(divisor: BigDecimal): BigDecimal;
		scale(): number;
		scaleByPowerOfTen(n: number): BigDecimal;
		setScale(newScale: number): BigDecimal;
		setScaleWithRoundingMode(newScale: number, roundingMode: number): BigDecimal;
		shortValueExact(): number;
		signum(): number;
		stripTrailingZeros(): BigDecimal;
		subtract(subtrahend: BigDecimal): BigDecimal;
		toEngineeringString(): string;
		toPlainString(): string;
		toString(): string;
		ulp(): BigDecimal;
	}

	// class java.lang.Byte
	interface Byte extends Comparable<Byte> {}
	class Byte {
		constructor(value: number);
		static MIN_VALUE: number;
		static MAX_VALUE: number;
		static SIZE: number;
		static BYTES: number;
		static compare(x: number, y: number): number;
		static decode(s: string): Byte;
		static hashCode(b: number): number;
		static parseByte(s: string): number;
		static parseByteWithRadix(s: string, radix: number): number;
		static toString(b: number): string;
		static valueOf(s: string): Byte;
		static valueOfByte(b: number): Byte;
		static valueOfWithRadix(s: string, radix: number): Byte;
		byteValue(): number;
		compareTo(b: Byte): number;
		doubleValue(): number;
		equals(o: any): boolean;
		floatValue(): number;
		hashCode(): number;
		intValue(): number;
		longValue(): Long;
		shortValue(): number;
		toString(): string;
	}

	// class java.lang.Character
	interface Character extends Comparable<Character> {}
	class Character {
		constructor(value: number);
		static MIN_RADIX: number;
		static MAX_RADIX: number;
		static MIN_VALUE: number;
		static MAX_VALUE: number;
		static MIN_SURROGATE: number;
		static MAX_SURROGATE: number;
		static MIN_LOW_SURROGATE: number;
		static MAX_LOW_SURROGATE: number;
		static MIN_HIGH_SURROGATE: number;
		static MAX_HIGH_SURROGATE: number;
		static MIN_SUPPLEMENTARY_CODE_POINT: number;
		static MIN_CODE_POINT: number;
		static MAX_CODE_POINT: number;
		static SIZE: number;
		static BYTES: number;
		static charCount(codePoint: number): number;
		static codePointAt(a: number[], index: number): number;
		static codePointAtSequence(seq: CharSequence, index: number): number;
		static codePointAtWithLimit(a: number[], index: number, limit: number): number;
		static codePointBefore(a: number[], index: number): number;
		static codePointBeforeSequence(cs: CharSequence, index: number): number;
		static codePointBeforeWithStart(a: number[], index: number, start: number): number;
		static codePointCount(a: number[], offset: number, count: number): number;
		static codePointCountSequence(seq: CharSequence, beginIndex: number, endIndex: number): number;
		static compare(x: number, y: number): number;
		static digit(c: number, radix: number): number;
		static forDigit(digit: number, radix: number): number;
		static hashCode(c: number): number;
		static isBmpCodePoint(codePoint: number): boolean;
		static isDigit(c: number): boolean;
		static isHighSurrogate(ch: number): boolean;
		static isLetter(c: number): boolean;
		static isLetterOrDigit(c: number): boolean;
		static isLowSurrogate(ch: number): boolean;
		static isLowerCase(c: number): boolean;
		static isSpace(c: number): boolean;
		static isSupplementaryCodePoint(codePoint: number): boolean;
		static isSurrogatePair(highSurrogate: number, lowSurrogate: number): boolean;
		static isUpperCase(c: number): boolean;
		static isValidCodePoint(codePoint: number): boolean;
		static isWhitespace(ch: number): boolean;
		static isWhitespaceInt(codePoint: number): boolean;
		static offsetByCodePoints(a: number[], start: number, count: number, index: number, codePointOffset: number): number;
		static offsetByCodePointsSequence(seq: CharSequence, index: number, codePointOffset: number): number;
		static toChars(codePoint: number): number[];
		static toCharsWithDst(codePoint: number, dst: number[], dstIndex: number): number;
		static toCodePoint(highSurrogate: number, lowSurrogate: number): number;
		static toLowerCase(c: number): number;
		static toString(x: number): string;
		static toUpperCase(c: number): number;
		static valueOf(c: number): Character;
		charValue(): number;
		compareTo(c: Character): number;
		equals(o: any): boolean;
		hashCode(): number;
		toString(): string;
	}

	// class java.lang.Class
	class Class<T> {
		constructor();
	}

	// class java.util.stream.Collectors
	class Collectors {
		static collectingAndThen<T, A, R, RR>(downstream: Collector<T, A, R>, finisher: Function<R, RR>): Collector<T, A, RR>;
		static collectingAndThenJs<T, A, R, RR>(downstream: Collector<T, A, R>, finisher: (t: R) => RR): Collector<T, A, RR>;
		static counting<T>(): Collector<T, any, Long>;
		static groupingBy<T, K, A, D>(classifier: Function<T, K>, downstream: Collector<T, A, D>): Collector<T, any, Map<K, D>>;
		static groupingByAsLists<T, K>(classifier: Function<T, K>): Collector<T, any, Map<K, List<T>>>;
		static groupingByAsListsJs<T, K>(classifier: (t: T) => K): Collector<T, any, Map<K, List<T>>>;
		static groupingByJs<T, K, A, D>(classifier: (t: T) => K, downstream: Collector<T, A, D>): Collector<T, any, Map<K, D>>;
		static groupingByToMap<T, K, D, A, M extends Map<K, D>>(classifier: Function<T, K>, mapFactory: Supplier<M>, downstream: Collector<T, A, D>): Collector<T, any, M>;
		static groupingByToMapJs<T, K, D, A, M extends Map<K, D>>(classifier: (t: T) => K, mapFactory: () => M, downstream: Collector<T, A, D>): Collector<T, any, M>;
		static joining(): Collector<CharSequence, any, string>;
		static joiningWithDelimiter(delimiter: CharSequence): Collector<CharSequence, any, string>;
		static joiningWithDelimiterPrefixSuffix(delimiter: CharSequence, prefix: CharSequence, suffix: CharSequence): Collector<CharSequence, any, string>;
		static mapping<T, U, A, R>(mapper: Function<T, U>, downstream: Collector<U, A, R>): Collector<T, any, R>;
		static mappingJs<T, U, A, R>(mapper: (t: T) => U, downstream: Collector<U, A, R>): Collector<T, any, R>;
		static maxBy<T>(comparator: Comparator<T>): Collector<T, any, Optional<T>>;
		static minBy<T>(comparator: Comparator<T>): Collector<T, any, Optional<T>>;
		static partitioningBy<T, D, A>(predicate: Predicate<T>, downstream: Collector<T, A, D>): Collector<T, any, Map<boolean, D>>;
		static partitioningByAsLists<T>(predicate: Predicate<T>): Collector<T, any, Map<boolean, List<T>>>;
		static partitioningByAsListsJs<T>(predicate: (t: T) => boolean): Collector<T, any, Map<boolean, List<T>>>;
		static partitioningByJs<T, D, A>(predicate: (t: T) => boolean, downstream: Collector<T, A, D>): Collector<T, any, Map<boolean, D>>;
		static toCollection<T, C extends Collection<T>>(collectionFactory: Supplier<C>): Collector<T, any, C>;
		static toCollectionJs<T, C extends Collection<T>>(collectionFactory: () => C): Collector<T, any, C>;
		static toList<T>(): Collector<T, any, List<T>>;
		static toMap<T, K, U>(keyMapper: Function<T, K>, valueMapper: Function<T, U>, mergeFunction: BinaryOperator<U>): Collector<T, any, Map<K, U>>;
		static toMapJs<T, K, U>(keyMapper: (t: T) => K, valueMapper: (t: T) => U, mergeFunction: (t: U, u: U) => U): Collector<T, any, Map<K, U>>;
		static toMapSuppliedBy<T, K, U, M extends Map<K, U>>(keyMapper: Function<T, K>, valueMapper: Function<T, U>, mergeFunction: BinaryOperator<U>, mapSupplier: Supplier<M>): Collector<T, any, M>;
		static toMapSuppliedByJs<T, K, U, M extends Map<K, U>>(keyMapper: (t: T) => K, valueMapper: (t: T) => U, mergeFunction: (t: U, u: U) => U, mapSupplier: () => M): Collector<T, any, M>;
		static toMapUniquely<T, K, U>(keyMapper: Function<T, K>, valueMapper: Function<T, U>): Collector<T, any, Map<K, U>>;
		static toMapUniquelyJs<T, K, U>(keyMapper: (t: T) => K, valueMapper: (t: T) => U): Collector<T, any, Map<K, U>>;
		static toSet<T>(): Collector<T, any, Set<T>>;
	}

	// class java.util.Date
	interface Date extends Comparable<Date> {}
	class Date {
		static UTC(year: number, month: number, date: number, hrs: number, min: number, sec: number): Long;
		static now(): Long;
		protected static padTwo(number: number): string;
		static parse(s: string): Long;
		after(when: Date): boolean;
		before(when: Date): boolean;
		clone(): any;
		compareTo(other: Date): number;
		dateValue(): Date;
		equals(obj: any): boolean;
		getDate(): number;
		getDay(): number;
		getFullYear(): number;
		getHours(): number;
		getMilliseconds(): number;
		getMinutes(): number;
		getMonth(): number;
		getSeconds(): number;
		getTime(): Long;
		getTimezoneOffset(): number;
		getUTCDate(): number;
		getUTCDay(): number;
		getUTCFullYear(): number;
		getUTCHours(): number;
		getUTCMilliseconds(): number;
		getUTCMinutes(): number;
		getUTCMonth(): number;
		getUTCSeconds(): number;
		getYear(): number;
		hashCode(): number;
		setDate(date: number): void;
		setFullYear(year: number): void;
		setFullYearDay(year: number, month: number, day: number): void;
		setHours(hours: number): void;
		setHoursTime(hours: number, mins: number, secs: number, ms: number): void;
		setMilliseconds(milliseconds: number): void;
		setMinutes(minutes: number): void;
		setMonth(month: number): void;
		setSeconds(seconds: number): void;
		setTime(time: Long): void;
		setUTCDate(day: number): void;
		setUTCFullYear(year: number): void;
		setUTCHours(hours: number): void;
		setUTCMilliseconds(milliseconds: number): void;
		setUTCMinutes(minutes: number): void;
		setUTCMonth(month: number): void;
		setUTCSeconds(secs: number): void;
		setYear(year: number): void;
		toDateString(): string;
		toGMTString(): string;
		toISOString(): string;
		toJSON(): string;
		toLocaleDateString(): string;
		toLocaleString(): string;
		toLocaleTimeString(): string;
		toString(): string;
		toTimeString(): string;
		toUTCString(): string;
		valueOf(): Long;
	}

	// class java.lang.Enum
	interface Enum<E extends Enum<E>> extends Comparable<E> {}
	class Enum<E extends Enum<E>> {
		protected static createValueOfMap<T extends Enum<T>>(enumConstants: T[]): any;
		protected static valueOf<T extends Enum<T>>(map: any, name: string): T;
		compareTo(other: E): number;
		equals(other: any): boolean;
		hashCode(): number;
		name(): string;
		ordinal(): number;
		toString(): string;
	}

	// class java.lang.Exception
	class Exception extends Throwable {
	}

	// class java.lang.Float
	interface Float extends Comparable<Float> {}
	class Float {
		constructor(value: number);
		static MAX_VALUE: number;
		static MIN_VALUE: number;
		static MAX_EXPONENT: number;
		static MIN_EXPONENT: number;
		static MIN_NORMAL: number;
		static NaN: number;
		static NEGATIVE_INFINITY: number;
		static POSITIVE_INFINITY: number;
		static SIZE: number;
		static BYTES: number;
		static compare(x: number, y: number): number;
		static floatToIntBits(value: number): number;
		static hashCode(f: number): number;
		static intBitsToFloat(bits: number): number;
		static isFinite(x: number): boolean;
		static isInfinite(x: number): boolean;
		static isNaN(x: number): boolean;
		static max(a: number, b: number): number;
		static min(a: number, b: number): number;
		static parseFloat(s: string): number;
		static sum(a: number, b: number): number;
		static toString(b: number): string;
		static valueOf(s: string): Float;
		static valueOfFloat(f: number): Float;
		byteValue(): number;
		compareTo(b: Float): number;
		doubleValue(): number;
		equals(o: any): boolean;
		floatValue(): number;
		hashCode(): number;
		intValue(): number;
		isInfinite(): boolean;
		isNaN(): boolean;
		longValue(): Long;
		shortValue(): number;
		toString(): string;
		valueOf(): number;
	}

	// class java.io.InputStream
	class InputStream {
		constructor();
		available(): number;
		close(): void;
		mark(readlimit: number): void;
		markSupported(): boolean;
		read(): number;
		readBuffer(buffer: number[]): number;
		readBufferOffset(buffer: number[], byteOffset: number, byteCount: number): number;
		reset(): void;
		skip(byteCount: Long): Long;
	}

	// class java.lang.Integer
	interface Integer extends Comparable<Integer> {}
	class Integer {
		constructor(value: number);
		static MAX_VALUE: number;
		static MIN_VALUE: number;
		static SIZE: number;
		static BYTES: number;
		static bitCount(x: number): number;
		static compare(x: number, y: number): number;
		static decode(s: string): Integer;
		static hashCode(i: number): number;
		static highestOneBit(i: number): number;
		static lowestOneBit(i: number): number;
		static max(a: number, b: number): number;
		static min(a: number, b: number): number;
		static numberOfLeadingZeros(i: number): number;
		static numberOfTrailingZeros(i: number): number;
		static parseInt(s: string): number;
		static parseIntWithRadix(s: string, radix: number): number;
		static reverse(i: number): number;
		static reverseBytes(i: number): number;
		static rotateLeft(i: number, distance: number): number;
		static rotateRight(i: number, distance: number): number;
		static signum(i: number): number;
		static sum(a: number, b: number): number;
		static toBinaryString(value: number): string;
		static toHexString(value: number): string;
		static toOctalString(value: number): string;
		static toString(value: number): string;
		static toStringWithRadix(value: number, radix: number): string;
		static valueOf(s: string): Integer;
		static valueOfInt(i: number): Integer;
		static valueOfWithRadix(s: string, radix: number): Integer;
		byteValue(): number;
		compareTo(b: Integer): number;
		doubleValue(): number;
		equals(o: any): boolean;
		floatValue(): number;
		hashCode(): number;
		intValue(): number;
		longValue(): Long;
		shortValue(): number;
		toString(): string;
		valueOf(): number;
	}

	// class java.lang.Long
	interface Long extends Comparable<Long> {}
	class Long {
		constructor(value: Long);
		static MAX_VALUE: Long;
		static MIN_VALUE: Long;
		static SIZE: number;
		static BYTES: number;
		static bitCount(i: Long): number;
		static compare(x: Long, y: Long): number;
		static decode(s: string): Long;
		static hashCode(l: Long): number;
		static highestOneBit(i: Long): Long;
		static lowestOneBit(i: Long): Long;
		static max(a: Long, b: Long): Long;
		static min(a: Long, b: Long): Long;
		static numberOfLeadingZeros(i: Long): number;
		static numberOfTrailingZeros(i: Long): number;
		static parseLong(s: string): Long;
		static parseLongWithRadix(s: string, radix: number): Long;
		static reverse(i: Long): Long;
		static reverseBytes(i: Long): Long;
		static rotateLeft(i: Long, distance: number): Long;
		static rotateRight(i: Long, distance: number): Long;
		static signum(i: Long): number;
		static sum(a: Long, b: Long): Long;
		static toBinaryString(value: Long): string;
		static toHexString(value: Long): string;
		static toOctalString(value: Long): string;
		static toString(value: Long): string;
		static toStringWithRadix(value: Long, intRadix: number): string;
		static valueOf(s: string): Long;
		static valueOfLong(i: Long): Long;
		static valueOfWithRadix(s: string, radix: number): Long;
		byteValue(): number;
		compareTo(b: Long): number;
		doubleValue(): number;
		equals(o: any): boolean;
		floatValue(): number;
		hashCode(): number;
		intValue(): number;
		longValue(): Long;
		shortValue(): number;
		toString(): string;
		valueOf(): number;
	}

	// class java.util.Optional
	class Optional<T> {
		static empty<T>(): Optional<T>;
		static of<T>(value: T): Optional<T>;
		static ofNullable<T>(value: T): Optional<T>;
		equals(obj: any): boolean;
		filter(predicate: Predicate<T>): Optional<T>;
		filterJs(predicate: (t: T) => boolean): Optional<T>;
		flatMap<U>(mapper: Function<T, Optional<U>>): Optional<U>;
		flatMapJs<U>(mapper: (t: T) => Optional<U>): Optional<U>;
		get(): T;
		hashCode(): number;
		ifPresent(consumer: Consumer<T>): void;
		ifPresentJs(consumer: (t: T) => void): void;
		isPresent(): boolean;
		map<U>(mapper: Function<T, U>): Optional<U>;
		mapJs<U>(mapper: (t: T) => U): Optional<U>;
		orElse(other: T): T;
		orElseGet(other: Supplier<T>): T;
		orElseGetJs(other: () => T): T;
		orElseThrow<X extends Throwable>(exceptionSupplier: Supplier<X>): T;
		orElseThrowJs<X extends Throwable>(exceptionSupplier: () => X): T;
		toString(): string;
	}

	// class java.io.OutputStream
	class OutputStream {
		constructor();
		close(): void;
		flush(): void;
		write(oneByte: number): void;
		writeBuffer(buffer: number[]): void;
		writeBufferOffset(buffer: number[], offset: number, count: number): void;
	}

	// class java.lang.RuntimeException
	class RuntimeException extends Exception {
	}

	// class java.util.Stack
	interface Stack<E> extends List<E>, List<E>, Collection<E> {}
	class Stack<E> {
		constructor();
		clone(): any;
		empty(): boolean;
		peek(): E;
		pop(): E;
		push(o: E): E;
		search(o: any): number;
	}

	// class java.lang.StackTraceElement
	class StackTraceElement {
		constructor();
		equals(other: any): boolean;
		getClassName(): string;
		getFileName(): string;
		getLineNumber(): number;
		getMethodName(): string;
		hashCode(): number;
		isNativeMethod(): boolean;
		toString(): string;
	}

	// class java.lang.Throwable
	class Throwable {
		static of(e: any): Throwable;
		fillInStackTrace(): Throwable;
		getBackingJsObject(): any;
		getCause(): Throwable;
		getLocalizedMessage(): string;
		getMessage(): string;
		getStackTrace(): StackTraceElement[];
		getSuppressed(): Throwable[];
		initCause(cause: Throwable): Throwable;
		printStackTrace(): void;
		setStackTrace(stackTrace: StackTraceElement[]): void;
		toString(): string;
	}

	// class java.lang.Void
	class Void {
	}

}

declare namespace $tf.session {

	// interface com.google.gwt.user.client.rpc.AsyncCallback
	interface AsyncCallback<T> {
		onFailure(caught: $tf.Throwable): void;
		onSuccess(result: T): void;
	}

}

declare namespace $tf.view {

	// class com.google.gwt.core.client.JsDate
	class JsDate {
		static UTC(year: number, month: number, dayOfMonth: number, hours: number, minutes: number, seconds: number, millis: number): number;
		static create(): JsDate;
		static createByMilliseconds(milliseconds: number): JsDate;
		static createByString(dateString: string): JsDate;
		static createByYearAndMonth(year: number, month: number): JsDate;
		static createByYearMonthAndDay(year: number, month: number, dayOfMonth: number): JsDate;
		static createByYearMonthDayAndHour(year: number, month: number, dayOfMonth: number, hours: number): JsDate;
		static createByYearMonthDayHourAndMinute(year: number, month: number, dayOfMonth: number, hours: number, minutes: number): JsDate;
		static createByYearMonthDayHourMinuteAndSecond(year: number, month: number, dayOfMonth: number, hours: number, minutes: number, seconds: number): JsDate;
		static createByYearMonthDayHourMinuteSecondAndMilli(year: number, month: number, dayOfMonth: number, hours: number, minutes: number, seconds: number, millis: number): JsDate;
		static now(): number;
		static parse(dateString: string): number;
		getDate(): number;
		getDay(): number;
		getFullYear(): number;
		getHours(): number;
		getMilliseconds(): number;
		getMinutes(): number;
		getMonth(): number;
		getSeconds(): number;
		getTime(): number;
		getTimezoneOffset(): number;
		getUTCDate(): number;
		getUTCDay(): number;
		getUTCFullYear(): number;
		getUTCHours(): number;
		getUTCMilliseconds(): number;
		getUTCMinutes(): number;
		getUTCMonth(): number;
		getUTCSeconds(): number;
		getYear(): number;
		setDate(dayOfMonth: number): number;
		setFullYear(year: number): number;
		setFullYearByYearAndMonth(year: number, month: number): number;
		setFullYearByYearMonthAndDay(year: number, month: number, day: number): number;
		setHours(hours: number): number;
		setHoursByHourAndMinute(hours: number, mins: number): number;
		setHoursByHourMinuteAndSecond(hours: number, mins: number, secs: number): number;
		setHoursByHourMinuteSecondAndMilli(hours: number, mins: number, secs: number, ms: number): number;
		setMinutes(minutes: number): number;
		setMinutesByMinuteAndSecond(minutes: number, seconds: number): number;
		setMinutesByMinuteSecondAndMilli(minutes: number, seconds: number, millis: number): number;
		setMonth(month: number): number;
		setMonthByMonthAndDay(month: number, dayOfMonth: number): number;
		setSeconds(seconds: number): number;
		setSecondsBySecondAndMilli(seconds: number, millis: number): number;
		setTime(milliseconds: number): number;
		setUTCDate(dayOfMonth: number): number;
		setUTCFullYear(year: number): number;
		setUTCFullYearByYearAndMonth(year: number, month: number): number;
		setUTCFullYearByYearMonthAndDay(year: number, month: number, day: number): number;
		setUTCHours(hours: number): number;
		setUTCHoursByHourAndMinute(hours: number, mins: number): number;
		setUTCHoursByHourMinuteAndSecond(hours: number, mins: number, secs: number): number;
		setUTCHoursByHourMinuteSecondAndMilli(hours: number, mins: number, secs: number, ms: number): number;
		setUTCMinutes(minutes: number): number;
		setUTCMinutesByMinuteAndSecond(minutes: number, seconds: number): number;
		setUTCMinutesByMinuteSecondAndMilli(minutes: number, seconds: number, millis: number): number;
		setUTCMonth(month: number): number;
		setUTCMonthByMonthAndDay(month: number, dayOfMonth: number): number;
		setUTCSeconds(seconds: number): number;
		setUTCSecondsAndMillis(seconds: number, millis: number): number;
		setYear(year: number): number;
		toDateString(): string;
		toGMTString(): string;
		toLocaleDateString(): string;
		toLocaleString(): string;
		toLocaleTimeString(): string;
		toTimeString(): string;
		toUTCString(): string;
		valueOf(): number;
	}

}

